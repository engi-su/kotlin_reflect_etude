package components

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import components.sqa.CodeCoveragePlugin
import components.sqa.DetektLintPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import extensions.isAndroidApp
import extensions.isAndroidLib
import extensions.library
import extensions.libs
import extensions.plugin

class SQAPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            when {
                isAndroidApp() ->
                    configure<ApplicationExtension> {
                        lint(Lint::configure)
                        apply<CodeCoveragePlugin>()
                    }

                isAndroidLib() ->
                    configure<LibraryExtension> {
                        lint(Lint::configure)
                        apply<CodeCoveragePlugin>()
                    }

                else -> {
                    pluginManager.apply("android-native-lint".plugin(libs))
                    configure<Lint>(Lint::configure)
                }

            }
            apply<DetektLintPlugin>()
            //apply<SpotlessLintPlugin>()
            pluginManager.apply("internal.spotless")
            dependencies {
                "testImplementation"("konsist".library(libs))
            }
        }
    }
}

private fun Lint.configure() {
    xmlReport = true
    checkDependencies = true
}