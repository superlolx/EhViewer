package com.hippo.ehviewer.ui.screen

import androidx.collection.MutableLongSet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import com.ehviewer.core.model.BaseGalleryInfo
import com.ehviewer.core.util.withIOContext
import com.hippo.ehviewer.EhDB
import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.client.EhEngine
import com.hippo.ehviewer.client.data.FavListUrlBuilder
import com.hippo.ehviewer.ui.tools.foldToLoadResult
import kotlin.random.Random
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.tarsin.coroutines.runSuspendCatching

class FavoritesViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val mutex = Mutex()

    val urlBuilder by savedStateHandle.saved(MutableStateSerializer()) {
        mutableStateOf(FavListUrlBuilder(favCat = Settings.recentFavCat))
    }

    val localFavCount = EhDB.localFavCount

    val data = snapshotFlow { urlBuilder.value.isLocal }.flatMapLatest { isLocalFav ->
        if (isLocalFav) {
            Pager(PagingConfig(20, jumpThreshold = 40)) {
                val keywordNow = urlBuilder.value.keyword.orEmpty()
                if (keywordNow.isBlank()) {
                    EhDB.localFavLazyList
                } else {
                    EhDB.searchLocalFav(keywordNow)
                }
            }.flow.map { data -> data.map<_, BaseGalleryInfo> { it } }
        } else {
            Pager(PagingConfig(DEFAULT_PAGE_SIZE, prefetchDistance = 20)) {
                object : PagingSource<String, BaseGalleryInfo>() {
                    override fun getRefreshKey(state: PagingState<String, BaseGalleryInfo>): String? = null
                    override suspend fun load(params: LoadParams<String>) = withIOContext {
                        val url = mutex.withLock {
                            with(urlBuilder.value) {
                                when (params) {
                                    is LoadParams.Prepend -> setIndex(params.key, isNext = false)
                                    is LoadParams.Append -> setIndex(params.key, isNext = true)
                                    is LoadParams.Refresh -> params.key?.let { setIndex(it, false) }
                                }
                                build()
                            }
                        }
                        runSuspendCatching {
                            EhEngine.getFavorites(url)
                        }.foldToLoadResult { result ->
                            Settings.favCat = result.catArray.toTypedArray()
                            Settings.favCount = result.countArray.toIntArray()
                            Settings.favCloudCount = result.countArray.sum()
                            LoadResult.Page(result.galleryInfoList, result.prev, result.next)
                        }
                    }
                }
            }.flow.map { data ->
                // https://github.com/FooIbar/EhViewer/issues/1190
                // Workaround for duplicate items when sorting by favorited time
                val gidSet = MutableLongSet(DEFAULT_PAGE_SIZE)
                data.filter { gidSet.add(it.gid) }
            }
        }
    }.cachedIn(viewModelScope)

    /**
     * Picks a random cloud favorite using GID-based seek.
     *
     * E-Hentai uses cursor-based pagination (GID positions, not page
     * numbers), so we can't ORDER BY RANDOM() or jump to page N.
     * Instead we:
     *
     * 1. Fetch first page → newest GID
     * 2. Fetch last page (prev=1-0) → oldest GID
     * 3. Pick a random GID in that range and navigate via next=<gid>
     * 4. Pick a random item from the returned page
     *
     * GIDs are monotonic, so a random GID maps to a random position.
     */
    suspend fun randomCloudFav(): BaseGalleryInfo? = withIOContext {
        val src = urlBuilder.value
        val first = EhEngine.getFavorites(src.copy(jumpTo = null, prev = null, next = null).build())
        val firstPage = first.galleryInfoList
        if (firstPage.isEmpty()) return@withIOContext null
        if (first.next == null) return@withIOContext firstPage.random()

        // Fetch last page via the prev=1-0 sentinel to get the oldest GID
        val last = runSuspendCatching {
            EhEngine.getFavorites(src.copy(jumpTo = null, prev = "1-0", next = null).build())
        }.getOrNull()

        val lastPage = last?.galleryInfoList
        if (lastPage.isNullOrEmpty()) return@withIOContext firstPage.random()

        // GIDs are monotonic and uniformly distributed → random GID = random position
        val minGid = lastPage.last().gid
        val maxGid = firstPage.first().gid
        val randomGid = (minGid..maxGid).random()

        // Seek to the page containing that GID using next=<gid>
        val middle = runSuspendCatching {
            EhEngine.getFavorites(src.copy(next = randomGid.toString(), prev = null, jumpTo = null).build())
        }.getOrNull()

        when {
            middle != null && middle.galleryInfoList.isNotEmpty() -> middle.galleryInfoList.random()
            Random.nextBoolean() -> lastPage.random()
            else -> firstPage.random()
        }
    }
}

private const val DEFAULT_PAGE_SIZE = 50
