@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("../build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins{
        id("com.gradle.enterprise") version "3.16.2" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() //for dependency like org.jlleitschuh.gradle.ktlint
    }
    versionCatalogs {
        create("libs") {
            from(files("../build-logic/gradle/libs.versions.toml"))
        }
    }
}

plugins {
    id("com.gradle.enterprise")
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}

rootProject.name = "core"
include(":jvm-lib")
