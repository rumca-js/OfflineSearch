package com.example.index

import com.example.index.data.AppConfigManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppConfigManagerTest {

    @Test
    fun testDefaultConfig() = runBlocking {
        val config = AppConfigManager.config.first()
        assertFalse(config.directLinks)
        assertFalse(config.showIcons)
    }

    @Test
    fun testUpdateDirectLinks() = runBlocking {
        AppConfigManager.setDirectLinks(true)
        val config = AppConfigManager.config.first()
        assertTrue(config.directLinks)
        
        // Reset for other tests if needed, though they run in parallel or sequence
        AppConfigManager.setDirectLinks(false)
    }

    @Test
    fun testUpdateShowIcons() = runBlocking {
        AppConfigManager.setShowIcons(true)
        val config = AppConfigManager.config.first()
        assertTrue(config.showIcons)
        
        AppConfigManager.setShowIcons(false)
    }
}
