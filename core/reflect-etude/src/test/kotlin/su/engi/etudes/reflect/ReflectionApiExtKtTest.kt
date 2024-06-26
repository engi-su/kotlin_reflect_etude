package su.engi.etudes.reflect

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.reflect.KProperty1

data class Simple(val str: String, val int: Int)
data class Parametrized<E> (val value: E)

data class Parametrized2<E> (val value: E)

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

    context ("read instance property"){
        should("read star projection"){
            data class Simple (val projected: Parametrized<String>)
            val s = Simple(Parametrized("Hello"))

            val sut = readInstanceProperty<Parametrized<*>>(s,"projected")
            sut.value shouldBe "Hello"
        }
    }

    should("return properties"){
        data class Simple (val one: Parametrized<String>,
                           val two: Parametrized<Int>,
                           val three: Int)
        val s = Simple(Parametrized("Hello"),
                       Parametrized(42),
                       24
        )
        val sut = getAllPropertiesOfType<Parametrized<*>>(s)

        sut.size shouldBe 2
        val sut2 = getAllPropertiesOfType2<Parametrized<*>>(s)
        sut2.size shouldBe 2
        sut2[0].shouldBeTypeOf<KProperty1<Any, Parametrized<*>>>()
        //@Suppress("UNCHECKED_CAST")
        //(sut2[0] as KProperty1<Any, Parametrized<*>>).get(s)
    }
    should("return names"){
        data class Simple (val one: Parametrized<String>,
                           val two: Parametrized<Int>,
                           val three: Int)
        val s = Simple(Parametrized("Hello"),
                       Parametrized(42),
                       24
        )
        val sut = getAllPropertyNamesOfType<Parametrized<*>>(s)
        sut.size shouldBe 2
        sut shouldContainExactlyInAnyOrder listOf("two", "one")
    }

    should("copy with new values"){
        data class Simple2 (val one: Parametrized<String>,
                           val two: Parametrized<Int>,
                           val three: Int)
        val s = Simple2(Parametrized("Hello"),
                       Parametrized(42),
                       24
        )
        val sut = s.updatePropertiesOfType<Parametrized<*>, Simple2>(Parametrized("GoodBy"), Parametrized(24))

        sut.one.value shouldBe "GoodBy"
        sut.two.value shouldBe 24

        /*data class Minor(val one: Int)
        val m = Minor (24)
        @Suppress("UNCHECKED_CAST")
        val f = m::class.declaredMembers.first().let{ it.returnType.classifier as KClass<String> }
        println("class ==== $f")*/
    }

})
