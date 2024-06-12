package su.engi.etudes.reflect

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

data class Simple(val str: String, val int: Int)
data class Parametrized<E> (val value: E)

data class Parametrized2<E> (val value: List<E>)

@JvmInline value class Name(val value: String)
class ReflectionApiExtKtTest : ShouldSpec({
    should("readInstanceProperty") {
        val data = Simple("Deep Thought", 42)
        val sut1 = readInstanceProperty<String>(data, "str")

        sut1 shouldBe "Deep Thought"

        val sut2 = readInstanceProperty<Int>(data, "int")

        sut2 shouldBe 42
    }

    context("copyDataObject") {
        should("copy simple data class") {
            val data = Simple("Deep Thought", 42)

            val sut = data.copyDataObject(data::int to 43)

            sut.int shouldBe 43
        }

        should("check receiver is data class") {

            val data = Name("Susan")
            val sut = shouldThrow<IllegalArgumentException>{
                data.copyDataObject(data::value to "Betty")
            }
            sut.message shouldContain "data class"
        }

        should("copy parametrized class"){
            val data = Parametrized("Hello")

            val sut = data.copyDataObject(data::value to "Bye bye")

            sut.value shouldBe "Bye bye"
        }

        should("copy parametrized 2 class"){
            val data = Parametrized2(Parametrized("Hello"))

            val sut = data.copyDataObject(data::value to Parametrized("Bye bye"))

            sut.value.value shouldBe "Bye bye"
        }
    }
})
