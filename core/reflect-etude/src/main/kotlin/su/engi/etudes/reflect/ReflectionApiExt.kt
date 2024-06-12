package su.engi.etudes.reflect

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * A function to read a property from an instance of a class given the property name
 * throws exception if property not found
 * ### Example:
 * ```
 * readInstanceProperty<Int>(sample, "age")
 * ```
 * requires: kotlin reflection
 * @throws exception if property not found
 * @see [StackOverflow](https://stackoverflow.com/a/35539628/11600358)
 */
@Suppress("UNCHECKED_CAST")
fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
    val property = instance::class.members
        // don't cast here to <Any, R>, it would succeed silently
        .first { it.name == propertyName } as KProperty1<Any, *>
    // force an invalid cast exception if incorrect type here
    return property.get(instance) as R
}

/**
 * The general invoke of [copy()](https://kotlinlang.org/docs/data-classes.html#copying)
 * of data class passing specific property values like you can do when calling copy directly:
 * ### Example:
 * ```
 * val person = Person("Jane", 7, Sex.FEMALE)
 * val copy = person.copyDataObject(
 *      person::name to "Jack",
 *      person::age to 93,
 *      person::sex to Sex.MALE
 * )
 * ```
 *
 * @receiver [T] generic of the data class
 * @param [properties] pairs of [T] property and new value to it
 * @return new instance of the same data class [T] *
 * @see [StackOverflow](https://stackoverflow.com/a/77579481/11600358)
 * @throws exception if receiver is not a data class,
 * if property name of is not listed in [T] data class constructor, if value type is
 * not correspond with property
 */
fun <T : Any> T.copyDataObject(vararg properties: Pair<KProperty<*>, Any?>): T {
    val dataClass = this::class
    require(dataClass.isData) { "Type of object to copy must be a data class" }
    val copyFunction = dataClass.memberFunctions.first { it.name == "copy" }
    val parameters = buildMap {
        put(copyFunction.instanceParameter!!, this@copyDataObject)
        properties.forEach { (property, value) ->
            val parameter = requireNotNull(
                copyFunction.parameters.firstOrNull { it.name == property.name },
            ) { "Parameter not found for property ${property.name}" }
            value?.let {
                require(
                    parameter.type.classifier == it::class, // Not supported parent in parameter
                ) { "Incompatible type of value for property ${property.name} ${parameter.type.classifier}" }
            }
            put(parameter, value)
        }
    }
    @Suppress("UNCHECKED_CAST")
    return copyFunction.callBy(parameters) as T
}

fun <D: Any> D.primaryConstructorPropertiesWithValues(): Map<KProperty1<D, *>, Any?> {
    val dataClass = this::class
    require(dataClass.isData) { "It is working only for data class" }
    val propertyNames = dataClass.primaryConstructor!!.parameters.map{ it.name }
    val properties = propertyNames.map { name ->
        dataClass.memberProperties.find { it.name == name }
    }
    val list = properties.map{it to it!!.get(this)}
    val result = buildMap<KProperty1<D,*>, Any?> {

    }
}
