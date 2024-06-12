package extensions

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

// ToDo consider internal
val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun String.version(libs: VersionCatalog): String = libs.findVersion(this).get().toString()
fun String.library(libs: VersionCatalog): Provider<MinimalExternalModuleDependency> = libs.findLibrary(this).get()

fun String.bundle(libs: VersionCatalog): Provider<ExternalModuleDependencyBundle> = libs.findBundle(this).get()
fun String.plugin(libs: VersionCatalog): String = libs.findPlugin(this).get().get().pluginId

fun Provider<MinimalExternalModuleDependency>.asString() = this.get().toString()