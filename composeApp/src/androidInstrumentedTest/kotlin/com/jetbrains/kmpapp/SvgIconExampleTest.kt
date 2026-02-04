package com.jetbrains.kmpapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jetbrains.kmpapp.examples.SvgIconExample
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SvgIconExampleTest : BaseComposeTest() {

    @Test
    fun testSvgIconExampleDisplaysAllElements() {
        composeTestRule.setContent {
            SvgIconExample()
        }

        composeTestRule.onNodeWithText("SVG Icon Example")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Home icon")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Search icon")
            .assertIsDisplayed()
    }
}
