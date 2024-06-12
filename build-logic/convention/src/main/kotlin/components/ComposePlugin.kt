package components

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import extensions.isAndroidApp
import extensions.isAndroidLib
import extensions.isAndroidTest
import extensions.library
import extensions.libs
import extensions.version

class ComposePlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            when {
                isAndroidApp() -> configureAndroidCompose(extensions.getByType<ApplicationExtension>())
                isAndroidLib() -> configureAndroidCompose(extensions.getByType<LibraryExtension>())
                isAndroidTest() -> configureAndroidCompose(extensions.getByType<TestExtension>())
                else -> throw IllegalStateException("Project can't support compose")
            }
        }
    }
    private fun Project.configureAndroidCompose(ext: CommonExtension<*, *, *, *, *, *>,) {
        ext.apply {
            buildFeatures {
                compose = true
            }

            composeOptions {
                kotlinCompilerExtensionVersion = "androidx-compose-compiler".version(libs)
            }

            defaultConfig {
                vectorDrawables {
                    useSupportLibrary = true
                }
            }
        }
        dependencies {
            val bom = "compose-bom".library(libs)
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            // Android Studio Preview support
            add("implementation", libs.findLibrary("ui-tooling-preview").get())
            add("debugImplementation", libs.findLibrary("ui-tooling").get())

            /*
            probably that is done in particular build.gradle.kts
            // Choose one of the following:
            // Material Design 3
            implementation("androidx.compose.material3:material3")
            // or Material Design 2
            implementation("androidx.compose.material:material")
            // or skip Material Design and build directly on top of foundational components
            implementation("androidx.compose.foundation:foundation")
            // or only import the main APIs for the underlying toolkit systems,
            // such as input and measurement/layout
            implementation("androidx.compose.ui:ui")
             */

            /** Optional
             *      // Optional - Included automatically by material, only add when you need
             *     // the icons but not the material library (e.g. when using Material3 or a
             *     // custom design system based on Foundation)
             *     implementation("androidx.compose.material:material-icons-core")
             *     // Optional - Add full set of material icons
             *     implementation("androidx.compose.material:material-icons-extended")
             *     // Optional - Add window size utils
             *     implementation("androidx.compose.material3:material3-window-size-class")
             *
             *     // Optional - Integration with activities
             *     implementation("androidx.activity:activity-compose:1.9.0")
             *     // Optional - Integration with ViewModels
             *     implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
             *     // Optional - Integration with LiveData
             *     implementation("androidx.compose.runtime:runtime-livedata")
             *     // Optional - Integration with RxJava
             *     implementation("androidx.compose.runtime:runtime-rxjava2")
             */
        }
        tasks.withType<KotlinCompile>().configureEach {
            kotlinOptions {
                freeCompilerArgs += buildComposeMetricsParameters()
                freeCompilerArgs += stabilityConfiguration()
            }
        }
    }


    // ToDo PS-81
    private fun Project.buildComposeMetricsParameters(): List<String> {
        return emptyList()
    }
    // ToDo PS-82
    private fun Project.stabilityConfiguration() = listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${project.rootDir.absolutePath}/compose_compiler_config.conf",
    )
}