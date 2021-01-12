package org.oxycblt.auxio.search

import android.content.Context
import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.oxycblt.auxio.R
import org.oxycblt.auxio.music.Album
import org.oxycblt.auxio.music.Artist
import org.oxycblt.auxio.music.BaseModel
import org.oxycblt.auxio.music.Genre
import org.oxycblt.auxio.music.Header
import org.oxycblt.auxio.music.MusicStore
import org.oxycblt.auxio.music.Song
import org.oxycblt.auxio.recycler.DisplayMode
import org.oxycblt.auxio.settings.SettingsManager

/**
 * The [ViewModel] for the search functionality
 * @author OxygenCobalt
 */
class SearchViewModel : ViewModel() {
    private val mSearchResults = MutableLiveData(listOf<BaseModel>())
    val searchResults: LiveData<List<BaseModel>> get() = mSearchResults

    private var mFilterMode = DisplayMode.SHOW_ALL
    val filterMode: DisplayMode get() = mFilterMode

    private var mLastQuery = ""

    private var mIsNavigating = false
    val isNavigating: Boolean get() = mIsNavigating

    private val musicStore = MusicStore.getInstance()
    private val settingsManager = SettingsManager.getInstance()

    init {
        mFilterMode = settingsManager.searchFilterMode
    }

    fun doSearch(query: String, context: Context) {
        mLastQuery = query

        if (query.isEmpty()) {
            mSearchResults.value = listOf()

            return
        }

        viewModelScope.launch {
            val results = mutableListOf<BaseModel>()

            if (mFilterMode.isAllOr(DisplayMode.SHOW_ARTISTS)) {
                musicStore.artists.filterByOrNull(query)?.let {
                    results.add(Header(id = -1, name = context.getString(R.string.label_artists)))
                    results.addAll(it)
                }
            }

            if (mFilterMode.isAllOr(DisplayMode.SHOW_ALBUMS)) {
                musicStore.albums.filterByOrNull(query)?.let {
                    results.add(Header(id = -2, name = context.getString(R.string.label_albums)))
                    results.addAll(it)
                }
            }

            if (mFilterMode.isAllOr(DisplayMode.SHOW_GENRES)) {
                musicStore.genres.filterByOrNull(query)?.let {
                    results.add(Header(id = -3, name = context.getString(R.string.label_genres)))
                    results.addAll(it)
                }
            }

            if (mFilterMode.isAllOr(DisplayMode.SHOW_SONGS)) {
                musicStore.songs.filterByOrNull(query)?.let {
                    results.add(Header(id = -4, name = context.getString(R.string.label_songs)))
                    results.addAll(it)
                }
            }

            mSearchResults.value = results
        }
    }

    fun updateFilterModeWithId(@IdRes id: Int, context: Context) {
        mFilterMode = DisplayMode.fromId(id)

        settingsManager.searchFilterMode = mFilterMode

        doSearch(mLastQuery, context)
    }

    private fun List<BaseModel>.filterByOrNull(value: String): List<BaseModel>? {
        val filtered = filter { it.name.contains(value, ignoreCase = true) }

        return if (filtered.isNotEmpty()) filtered else null
    }

    private fun List<BaseModel>.filterByDisplayMode(mode: DisplayMode): List<BaseModel> {
        return when (mode) {
            DisplayMode.SHOW_ALL -> this
            DisplayMode.SHOW_SONGS -> filterIsInstance<Song>()
            DisplayMode.SHOW_ALBUMS -> filterIsInstance<Album>()
            DisplayMode.SHOW_ARTISTS -> filterIsInstance<Artist>()
            DisplayMode.SHOW_GENRES -> filterIsInstance<Genre>()
        }
    }

    /**
     * Update the current navigation status
     * @param value Whether LibraryFragment is navigating or not
     */
    fun updateNavigationStatus(value: Boolean) {
        mIsNavigating = value
    }
}
