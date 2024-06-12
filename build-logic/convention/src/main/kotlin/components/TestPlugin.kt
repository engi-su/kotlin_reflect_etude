@file:Suppress("UnstableApiUsage")

package components

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import extensions.bundle
import extensions.isAndroidApp
import extensions.isAndroidLib
import extensions.isComposeLib
import extensions.isFeature
import extensions.isJvmApp
import extensions.isJvmLib
import extensions.library
import extensions.libs

class TestPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target){
            when {
                isAndroidApp() -> configureAndroidApp(extensions.getByType<ApplicationExtension>())
                isFeature() || isComposeLib() -> configureFeatureOrComposeLib(extensions.getByType<LibraryExtension>())
                isAndroidLib() -> configureAndroidLib(extensions.getByType<LibraryExtension>())
                isJvmLib() -> configureJvmLib()
                isJvmApp() -> configureJvmApp()
                else -> throw UnsupportedOperationException("Android or Jvm library or application plugin is missed")
            }
            dependencies {
                add("testImplementation", "kotest".bundle(libs))
            }
        }
    }
    /**
     * considered to be always compose
     */
    private fun Project.configureAndroidApp(ext: ApplicationExtension) {
        configureAndroidBase(ext)
        dependencies {
            add("androidTestImplementation", "ultron".bundle(libs))
        }
    }

    private fun Project.configureAndroidBase(ext: CommonExtension<*,*,*,*,*,*>) {
        ext.apply {
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            testOptions {
                unitTests {
                    // ToDo PS-94
                    isIncludeAndroidResources = true
                    // ToDo PS-95
                    isReturnDefaultValues = false /*default value*/
                    all {
                        it.useJUnitPlatform()
                    }
                }
            }
            dependencies {
                add("androidTestImplementation", "kotest-android".bundle(libs))
            }
        }
    }

    private fun Project.configureFeatureOrComposeLib(ext: LibraryExtension) {
        configureAndroidBase(ext)
        ext.apply {
            // ToDo for Ci/Cd only
            @Suppress("UnstableApiUsage")
            testOptions.animationsDisabled = true
        }
        dependencies{
            add("androidTestImplementation", "ultron".bundle(libs))
            add("debugImplementation", "ui-test-manifest".library(libs))
        }
    }

    private fun Project.configureAndroidLib(ext: LibraryExtension) {
        configureAndroidBase(ext)
    }
    private fun Project.configureJvmLib() {
        configureJvmBase()
    }

    private fun Project.configureJvmBase() {
        tasks.withType<Test>().configureEach{
            useJUnitPlatform()
        }
    }

    private fun Project.configureJvmApp() {
        configureJvmBase()
    }
}


