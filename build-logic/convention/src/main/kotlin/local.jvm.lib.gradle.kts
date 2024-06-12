import components.SQAPlugin
import components.TestPlugin

plugins{
    id("org.jetbrains.kotlin.jvm")
    `java-library`
}

apply<SQAPlugin>()
apply<TestPlugin>()
