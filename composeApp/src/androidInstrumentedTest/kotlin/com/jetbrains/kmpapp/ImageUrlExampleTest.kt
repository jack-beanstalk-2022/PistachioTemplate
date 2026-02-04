package com.jetbrains.kmpapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jetbrains.kmpapp.examples.ImageUrlExample
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageUrlExampleTest : BaseComposeTest() {

    @Test
    fun testImageUrlExampleDisplaysAll() {
        composeTestRule.setContent {
            ImageUrlExample()
        }

        composeTestRule.onNodeWithText("Image URL Example")
            .assertIsDisplayed()

        // Wait for the image to be displayed (node exists and has non-zero size)
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithContentDescription("Random image from Picsum")
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithContentDescription("Random image from Picsum")
            .assertIsDisplayed()
    }
}
