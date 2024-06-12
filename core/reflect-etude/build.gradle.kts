plugins {
    base
    alias(libs.plugins.local.jvm.lib)
}

group = "su.engi.etudes.reflect"

dependencies {
    implementation(libs.kotlin.reflect)
}
