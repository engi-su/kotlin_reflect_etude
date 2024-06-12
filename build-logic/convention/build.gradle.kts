@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    // Apply the Java Gradle convention development convention to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin convention to add support for Kotlin. Write Plugins
    `kotlin-dsl`
    // ...and test them with Groovy
    groovy
    // For demonstration purposes only.
    `maven-publish`

    //https://github.com/autonomousapps/gradle-best-practices-plugin
    id("com.autonomousapps.plugin-best-practices-plugin") version "0.10"
}

group = "ru.polescanner.build.logic"
version = "unspecified" //libs.versions.build.get()

// -Dimpl (for 'implementation')
val impl = providers.systemProperty("impl").orNull != null

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}


publishing {
    repositories {
        mavenLocal()
    }
}
val publishToMavenLocal = tasks.named("publishToMavenLocal")

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotest framework
            useJUnitJupiter()
            dependencies {
                // Equivalent to `testImplementation ...` in the
                // top-level dependencies block
                implementation(libs.kotest.runner)
                implementation(libs.kotest.assertion)
            }
        }

        // Create a new test suite
        val functionalTest by registering(JvmTestSuite::class) {
            // Use Groovy Spock test framework
            useSpock()
            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(project())
                implementation (libs.google.truth)
                implementation (libs.autonomousapps.testkit)
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure {
                        shouldRunAfter(test)
                        dependsOn(deleteOldFuncTests, publishToMavenLocal) //лекция на русском - Output

                        systemProperty("pluginVersion", "unspecified")//version)
                        systemProperty("impl", impl)

                        maxParallelForks = Runtime.getRuntime().availableProcessors() / 2

                        //overloads for AbstractTestTask beforeSuite, afterSuite, beforeTest, afterTest, and onOutput methods
                        //see https://github.com/gradle/gradle/issues/5431
                        beforeTest(
                            closureOf<TestDescriptor> { logger.lifecycle("Running test: $this") }
                        )
                        afterTest(
                            KotlinClosure2({ descriptor: TestDescriptor, result: TestResult ->
                                println("[${descriptor.className}] > ${descriptor.displayName}: ${result.resultType}")
                            })
                        )
                    }
                }
            }
        }
    }
}
// Ensure build/functionalTest doesn't grow without bound when tests sometimes fail to clean up
// after themselves.
val deleteOldFuncTests = tasks.register("deleteOldFuncTests", Delete::class) {
    delete(layout.buildDirectory.file("functionalTest"))
}


gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
    dependsOn(testing.suites.named("functionalTest"))
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

dependencies {
    compileOnly(libs.agp)
    runtimeOnly(libs.plugins.kotlin.dsl.asDependency())
    runtimeOnly(libs.plugins.android.library.asDependency())
    runtimeOnly(libs.plugins.android.application.asDependency())
    runtimeOnly(libs.plugins.kotlin.android.asDependency())

    implementation(libs.plugins.room.asDependency())
    implementation(libs.plugins.ksp.asDependency())

    compileOnly(libs.plugins.kotlin.android.asDependency())

    implementation(libs.plugins.detekt.asDependency())
    implementation(libs.plugins.spotless.asDependency())
    //implementation(libs.ktlint)

    implementation(libs.plugins.kover.asDependency())


}

// Groovy code can depend on Kotlin code

val compileFunctionalTestKotlin = tasks.named("compileFunctionalTestKotlin")
tasks.named("compileFunctionalTestGroovy", AbstractCompile::class) {
    dependsOn( compileFunctionalTestKotlin)
    classpath += files(compileFunctionalTestKotlin.get().outputs.files)
}

// Gradle plugins benefit from this argument
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-Xsam-conversions=class")
    }
}

gradlePlugin {
    plugins {
        register("detekt-lint") {
            id = "fortest.detekt"
            implementationClass = "fortest.ForTestRegisteredDetektLintPlugin"
        }
    }
}
//ToDo Private
fun Provider<PluginDependency>.asDependency() = this.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
infix fun dependencyOf(plugin: Provider<PluginDependency>) = plugin.asDependency()
