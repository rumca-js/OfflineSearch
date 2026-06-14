package com.example.index.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Singleton to manage app configuration.
 * Can be updated from various sources.
 */
object AppConfigManager {
    private val _config = MutableStateFlow(AppConfiguration())
    val config: StateFlow<AppConfiguration> = _config.asStateFlow()

    fun updateConfig(update: (AppConfiguration) -> AppConfiguration) {
        _config.update(update)
    }

    fun setDirectLinks(enabled: Boolean) {
        updateConfig { it.copy(directLinks = enabled) }
    }

    fun setShowIcons(enabled: Boolean) {
        updateConfig { it.copy(showIcons = enabled) }
    }
}
