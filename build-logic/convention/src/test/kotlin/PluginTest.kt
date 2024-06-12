import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.DefaultTask
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class PluginTest : ShouldSpec({

    should("apply script plugin") {
        // Create a test project and apply the plugin. NOT WORKING JAVA 17???
        val project =
            ProjectBuilder.builder().build()
        val taskName = "compileJava"//"detekt"
        val pluginName = "fortest.jvm.lib"
        project.pluginManager.apply(pluginName)

        project.pluginManager.hasPlugin(pluginName) shouldBe true
        project.tasks.findByName(taskName) shouldNotBe null
    }

    should("apply class unregistered plugin"){
        //val tmpFolder =
        //val projectFolder = tmpFolder.newFolder()
        val project = ProjectBuilder.builder()//.withProjectDir(projectFolder)
                .build()

        val taskName = "detekt"
        project.extensions.findByType<DetektExtension>() shouldNotBe null
        project.tasks.findByName(taskName) shouldNotBe null
    }

    should("apply class registered plugin"){
        val project = ProjectBuilder.builder()
            .build()

        val pluginName = "fortest.detekt"
        project.pluginManager.apply(pluginName)

        val taskName = "detekt"

        project.pluginManager.hasPlugin(pluginName) shouldBe true
        project.extensions.findByType<DetektExtension>() shouldNotBe null
        project.tasks.findByName(taskName).shouldBeInstanceOf<DefaultTask>()
        //more strict obj.shouldBeTypeOf<T>() where T exact type = Detekt?
    }
})