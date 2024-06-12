import extensions.asString
import extensions.isAndroidApp
import extensions.isComposeLib
import extensions.library
import extensions.libs
import extensions.version

plugins{
    id("com.diffplug.spotless")
}

spotless {
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
        ktlint("ktlint".version(libs))
            .customRuleSets(
                if(isComposeLib() || isAndroidApp()) {
                    listOf(
                        "ktlint-compose".library(libs).asString()
                    )
                } else emptyList()
            )
            .setEditorConfigPath("$rootDir/config/.editorconfig")
            .editorConfigOverride(
                mapOf(
                    "ktlint_code_style" to "intellij_idea"
                )
            )
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint("ktlint".version(libs))
    }

    //Don't add spotless as dependency for the Gradle's check task to facilitate separated codebase checks
    isEnforceCheck = false
}
