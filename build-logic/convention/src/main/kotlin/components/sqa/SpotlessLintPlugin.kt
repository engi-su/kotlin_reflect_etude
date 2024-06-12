package components.sqa

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import extensions.asString
import extensions.isAndroidApp
import extensions.isAndroidLib
import extensions.isComposeLib
import extensions.isFeature
import extensions.isJvmApp
import extensions.isJvmLib
import extensions.library
import extensions.libs
import extensions.plugin
import extensions.version

class SpotlessLintPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("spotless".plugin(libs))
            val extension = extensions.getByType<SpotlessExtension>()
            when {
                isAndroidApp() || isFeature() || isComposeLib()  -> configureSpotlessCompose(extension)
                isAndroidLib() || isJvmLib() || isJvmApp() -> configureSpotless(extension)
                else -> throw UnsupportedOperationException("Android or Jvm library or application plugin is missed")
            }
        }
    }
    private fun Project.configureSpotlessCompose(extension: SpotlessExtension) {
        configureSpotlessBase(extension)
        extension.apply {
            kotlin{
                ktlint("ktlint".version(libs))
                /*
                ToDo PS-106
                .setEditorConfigPath("$projectDir/config/.editorconfig")  // sample unusual placement
                   .editorConfigOverride(
                        mapOf(
                            "indent_size" to 2,
                        )
                    )
                 */

                    .customRuleSets(
                        listOf(
                            "ktlint-compose".library(libs).asString()
                        )
                    )
            }
        }
    }

    private fun Project.configureSpotless(extension: SpotlessExtension) {
        configureSpotlessBase(extension)
        extension.apply {
            kotlin {
                ktlint("ktlint".version(libs))
                    .setEditorConfigPath("$rootDir/config/.editorconfig")
                    .editorConfigOverride(
                        mapOf(
                            "ktlint_code_style" to "intellij_idea"
                        )
                    )
            }
        }
    }

    private fun Project.configureSpotlessBase(extension: SpotlessExtension) = extension.apply {
        java {
            target("src/*/java/**/*.java")
            googleJavaFormat().aosp()
            removeUnusedImports()
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }

        kotlin {
            target("src/*/kotlin/**/*.kt")
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }

        kotlinGradle {
            target("**/*.gradle.kts")
            ktlint("ktlint".version(libs))
        }

        // Don't add spotless as dependency for the Gradle's check task to facilitate separated codebase checks
        isEnforceCheck = false
    }

}

