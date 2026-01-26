import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.process.ExecOperations
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import com.android.build.gradle.AppExtension
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.test.ext.junit)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.rules)
            implementation(libs.espresso.core)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.material.icons.core)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
        }
    }
}

android {
    namespace = "com.jetbrains.kmpapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jetbrains.kmpapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Task to clear logcat before tests run
abstract class ClearLogcatTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
    @TaskAction
    fun run() {
        val adbPath = when {
            System.getenv("ANDROID_HOME") != null -> {
                val adb = project.file("${System.getenv("ANDROID_HOME")}/platform-tools/adb")
                if (adb.exists()) adb.absolutePath else "adb"
            }
            System.getenv("ANDROID_SDK_ROOT") != null -> {
                val adb = project.file("${System.getenv("ANDROID_SDK_ROOT")}/platform-tools/adb")
                if (adb.exists()) adb.absolutePath else "adb"
            }
            else -> "adb" // Assume adb is in PATH
        }
        
        execOperations.exec {
            commandLine(adbPath, "logcat", "-c")
            isIgnoreExitValue = true
        }
        println("Logcat cleared. Ready for test run.")
    }
}

tasks.register<ClearLogcatTask>("clearLogcat") {
    group = "verification"
    description = "Clears logcat buffer before test runs"
}

// Task to get error logs and screenshot from logcat
abstract class PullTestArtifactsTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
    @TaskAction
    fun run() {
        val androidExtension = project.extensions.getByName("android")
        val packageName = (androidExtension as? AppExtension)
            ?.defaultConfig?.applicationId ?: "com.jetbrains.kmpapp"
        
        // Try to find adb
        val adbPath = when {
            System.getenv("ANDROID_HOME") != null -> {
                val adb = project.file("${System.getenv("ANDROID_HOME")}/platform-tools/adb")
                if (adb.exists()) adb.absolutePath else "adb"
            }
            System.getenv("ANDROID_SDK_ROOT") != null -> {
                val adb = project.file("${System.getenv("ANDROID_SDK_ROOT")}/platform-tools/adb")
                if (adb.exists()) adb.absolutePath else "adb"
            }
            else -> "adb" // Assume adb is in PATH
        }
        
        // Get error logs from logcat filtered by package name, exceptions, and fatal errors
        // Use -t 5000 to get only the last 5000 lines (safety limit for last test run)
        val errorLogOutput = ByteArrayOutputStream()
        execOperations.exec {
            commandLine("sh", "-c", "$adbPath logcat -d -t 5000 *:E | grep -E \"$packageName| F \"")
            isIgnoreExitValue = true
            standardOutput = errorLogOutput
        }
        val errorLogs = errorLogOutput.toString("UTF-8")
        if (errorLogs.isNotBlank()) {
            println("ERROR_LOGS_START")
            println(errorLogs)
            println("ERROR_LOGS_END")
        }
    }
}

tasks.register<PullTestArtifactsTask>("pullTestArtifacts") {
    group = "verification"
    description = "Gets error logs from logcat and prints them"
}

// Make clearLogcat run before connectedAndroidTest tasks, and pullTestArtifacts run after
tasks.matching { it.name.startsWith("connected") && it.name.endsWith("AndroidTest") }.configureEach {
    dependsOn("clearLogcat")
    finalizedBy("pullTestArtifacts")
}
