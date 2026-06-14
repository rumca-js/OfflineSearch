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

    val filteredData by derivedStateOf {
        if (searchQuery.length < 2) {
            emptyList()
        } else {
            allPlaces.filter { place ->
                place.title?.contains(searchQuery, ignoreCase = true) == true ||
                place.description?.contains(searchQuery, ignoreCase = true) == true ||
                place.tags?.any { it.contains(searchQuery, ignoreCase = true) } == true
            }.take(50)
        }
    }
}
