
import gradle.kotlin.dsl.accessors._6ebf0b5d05ec1eff67605a516c5db18b.kotlin

plugins{
    id("org.jetbrains.kotlin.jvm")
    application
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        //ToDo PS-74
        allWarningsAsErrors = true
        freeCompilerArgs.addAll(
            // Enable experimental coroutines APIs, including Flow
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}