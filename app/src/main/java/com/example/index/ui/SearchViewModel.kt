package com.example.index.ui

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.index.ui.screens.Place
import com.example.index.ui.screens.loadPlacesFromAssets
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    var searchQuery by mutableStateOf("")
    var showSuggestions by mutableStateOf(false)
    var activeSearchQuery by mutableStateOf("")
    var searchHistory by mutableStateOf<List<String>>(emptyList())
    var allPlaces by mutableStateOf<List<Place>>(emptyList())
    var isLoading by mutableStateOf(true)
        private set

    var selectedPlace by mutableStateOf<Place?>(null)

    private var hasLoadedData = false

    fun loadDataIfNeeded(context: Context) {
        if (hasLoadedData) return
        
        viewModelScope.launch {
            isLoading = true
            allPlaces = loadPlacesFromAssets(context).sortedByDescending { it.page_rating_votes ?: 0 }
            isLoading = false
            hasLoadedData = true
        }
    }

    fun performSearch() {
        showSuggestions = false
        if (searchQuery.isNotBlank()) {
            activeSearchQuery = searchQuery
            val currentHistory = searchHistory.toMutableList()
            currentHistory.remove(searchQuery)
            currentHistory.add(0, searchQuery)
            if (currentHistory.size > 100) {
                searchHistory = currentHistory.take(100)
            } else {
                searchHistory = currentHistory
            }
        }
    }

    val suggestions by derivedStateOf {
        if (!showSuggestions || searchQuery.isEmpty()) {
            emptyList()
        } else {
            searchHistory.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredData by derivedStateOf {
        if (activeSearchQuery.length < 2) {
            emptyList()
        } else {
            allPlaces.filter { place ->
                place.title?.contains(activeSearchQuery, ignoreCase = true) == true ||
                place.description?.contains(activeSearchQuery, ignoreCase = true) == true ||
                place.link?.contains(activeSearchQuery, ignoreCase = true) == true ||
                place.tags?.any { it.contains(activeSearchQuery, ignoreCase = true) } == true
            }.take(50)
        }
    }
}
