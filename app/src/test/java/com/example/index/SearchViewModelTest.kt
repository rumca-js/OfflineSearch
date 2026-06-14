package com.example.index

import com.example.index.ui.SearchViewModel
import org.junit.Assert.*
import org.junit.Test

class SearchViewModelTest {

    @Test
    fun testSuggestionsVisibility() {
        val viewModel = SearchViewModel()
        
        // Initial state
        assertTrue(viewModel.suggestions.isEmpty())
        assertFalse(viewModel.showSuggestions)

        // Type something
        viewModel.searchQuery = "test"
        viewModel.showSuggestions = true
        
        // Add to history to have suggestions
        viewModel.performSearch() // This will hide suggestions but add "test" to history
        assertFalse(viewModel.showSuggestions)
        assertTrue(viewModel.suggestions.isEmpty())

        // Type again
        viewModel.searchQuery = "te"
        viewModel.showSuggestions = true
        
        // Suggestions should be visible now
        assertFalse(viewModel.suggestions.isEmpty())
        assertEquals("test", viewModel.suggestions[0])

        // Perform search (simulating clicking a suggestion or pressing search)
        viewModel.performSearch()
        
        // Suggestions should be hidden
        assertFalse(viewModel.showSuggestions)
        assertTrue(viewModel.suggestions.isEmpty())
    }
}
