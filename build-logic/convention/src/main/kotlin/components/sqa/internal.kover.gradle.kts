plugins {
    id("org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "kotlinx.kover.examples.merged.utils.*",
                    "kotlinx.kover.examples.merged.subproject.utils.*"
                )
            }
            includes {
                classes("kotlinx.kover.examples.merged.*")
            }
        }
        verify {
            rule("Minimal line coverage rate in percents") {
                bound {
                    minValue.set(50)
                    maxValue.set(75)
                }
            }
        }
    }
    currentProject {
        createVariant("custom") {
            /**
             * Tests, sources, classes, and compilation tasks of the 'app1AppDebug' build variant will be included in the report variant `custom`.
             * Thus, information from the 'app1AppDebug' variant will be included in the 'custom' report for this project and any project that specifies this project as a dependency.
             */
            addWithDependencies("app1AppDebug")
        }
    }
}