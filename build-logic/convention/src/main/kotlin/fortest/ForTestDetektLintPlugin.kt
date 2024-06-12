package fortest

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import extensions.isAndroidApp
import extensions.isAndroidLib
import extensions.isComposeLib
import extensions.isFeature
import extensions.isJvmApp
import extensions.isJvmLib

class ForTestDetektLintPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")//"detekt".plugin(libs))
            val extension = extensions.getByType<DetektExtension>()
            when {
                isAndroidApp() || isFeature() || isComposeLib()  -> configureDetektCompose(extension)
                isAndroidLib() || isJvmLib() || isJvmApp() -> configureDetekt(extension)
                else -> configureDetekt(extension) //throw UnsupportedOperationException("Android or Jvm library or application plugin is missed")
            }

            // Configure jvmTarget for gradle task `detekt`
            tasks.withType<Detekt>().configureEach {
                jvmTarget = JavaVersion.VERSION_17.toString()
            }

            // Configure jvmTarget for gradle task `detektGenerateBaseline`
            tasks.withType<DetektCreateBaselineTask>().configureEach {
                jvmTarget = JavaVersion.VERSION_17.toString()
            }
        }
    }
    private fun Project.configureDetektCompose(extension: DetektExtension) {
        configureDetektBase(extension)
//        dependencies {
//            add("detektPlugins", "detekt-compose".library(libs))
//        }
    }
    private fun Project.configureDetekt(extension: DetektExtension) {
        configureDetektBase(extension)
    }

    private fun Project.configureDetektBase(extension: DetektExtension) = extension.apply {
        buildUponDefaultConfig = true // preconfigure defaults.
        allRules = false // activate all available (even unstable) rules.
        autoCorrect = false // To enable or disable auto formatting.
        parallel = true // To enable or disable parallel execution of detekt on multiple submodules.
        config.setFrom("${rootDir}/config/detekt/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior.
        baseline = file("${rootDir}/config/detekt/detekt-baseline.xml") // a way of suppressing issues before introducing detekt.
        tasks.named<Detekt>("detekt") {
            reports {
                xml.required.set(true)
                // observe findings in your browser with structure and code snippets
                html.required.set(true)
                // similar to the console output, contains issue signature to manually edit baseline files
                txt.required.set(true)
                sarif.required.set(true)
                // simple Markdown format
                md.required.set(true)
            }
        }
        dependencies {
            // ToDo Consider using Detekt Formatting (ktlint) - there are complains of support - we use Spotless with ktlint instead
            //add("detektPlugins", "detekt-formatting".library(libs))
        }
    }

}






