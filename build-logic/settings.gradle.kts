@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins{
        id("com.gradle.enterprise") version "3.15.1" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() //for dependency like org.jlleitschuh.gradle.ktlint
    }
}

plugins {
    id("com.gradle.enterprise")
    //id("com.gradle.common-custom-user-data-gradle-plugin") version "2.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "build-logic"
include(":convention")
