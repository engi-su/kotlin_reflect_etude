package extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal fun Project.isAndroidApp() = pluginManager.hasPlugin("com.android.application")
internal fun Project.isAndroidLib() = pluginManager.hasPlugin("com.android.library")
internal fun Project.isAndroidTest() = pluginManager.hasPlugin("com.android.test")
internal fun Project.isComposeLib() = pluginManager.hasPlugin("local.compose.lib")
internal fun Project.isFeature() = pluginManager.hasPlugin("local.feature")

internal fun Project.isAndroidModule() = isAndroidLib() || isAndroidApp() || isAndroidTest()
internal fun Project.isJvmLib() = pluginManager.hasPlugin("java-library")
internal fun Project.isJvmApp() = pluginManager.hasPlugin("application")

internal val Project.androidAppExtension: CommonExtension<*, *, *, *, *, *>
    get() = extensions.getByType<ApplicationExtension>()

internal val Project.androidLibExtension: CommonExtension<*, *, *, *, *, *>
    get() = extensions.getByType<LibraryExtension>()

internal val Project.androidExtension: CommonExtension<*, *, *, *, *, *>
    get() = runCatching { androidLibExtension }
        .recoverCatching { androidAppExtension }
        .onFailure { println("Could not find Android Library or Android Application extension from this project") }
        .getOrThrow()