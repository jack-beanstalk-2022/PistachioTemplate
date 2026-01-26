package com.jetbrains.kmpapp

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AndroidInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val testName = TestName()

    private var videoFile: File? = null

    @Before
    fun setUp() {
        startVideoRecording()
    }

    @After
    fun tearDown() {
        stopVideoRecording()
    }

    @Test
    fun testScrollingDownGesture() {
        // Wait for the activity to be fully loaded
        Thread.sleep(2000)

        // Perform swipe up gesture to simulate scrolling down
        Espresso.onView(isRoot()).perform(ViewActions.swipeUp())

        // Wait a bit for the scroll to complete
        Thread.sleep(1000)
    }

    private fun startVideoRecording() {
        try {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val context = instrumentation.targetContext

            // Get external files directory
            val externalFilesDir = context.getExternalFilesDir(null) ?: return

            // Generate filename with test name
            val testMethodName = testName.methodName
            videoFile = File(externalFilesDir, "screenrecord_${testMethodName}.mp4")
            
            // Delete existing file if it exists to allow overwriting
            if (videoFile!!.exists()) {
                videoFile!!.delete()
            }

            // Start recording using adb shell screenrecord
            // Note: screenrecord has a default 3-minute limit which is plenty for a test
            instrumentation.uiAutomation.executeShellCommand("screenrecord ${videoFile!!.absolutePath}")
        } catch (e: Exception) {
            android.util.Log.e("AndroidInstrumentedTest", "Failed to start video recording", e)
        }
    }

    private fun stopVideoRecording() {
        try {
            // Send SIGINT to screenrecord to stop it gracefully so the MP4 header is written correctly
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pkill -2 screenrecord")
            
            // Give it a moment to finish writing the file
            Thread.sleep(500)

            videoFile?.let {
                android.util.Log.d("AndroidInstrumentedTest", "Video saved to: ${it.absolutePath}")
            }
        } catch (e: Exception) {
            android.util.Log.e("AndroidInstrumentedTest", "Failed to stop video recording", e)
        }
    }
}
