package com.jetbrains.kmpapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jetbrains.kmpapp.examples.MapExample
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapExampleTest : BaseComposeTest() {

    @Test
    fun testMapExampleDisplaysAllElements() {
        composeTestRule.setContent {
            MapExample()
        }

        composeTestRule.onNodeWithText("Map Example")
            .assertIsDisplayed()

        // Wait for the map to be displayed (GoogleMap loads asynchronously)
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithTag("map_view")
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
