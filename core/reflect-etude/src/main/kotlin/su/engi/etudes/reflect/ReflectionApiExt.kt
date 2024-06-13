package su.engi.etudes.reflect

import kotlin.reflect.*
import kotlin.reflect.full.*

/**
 * A function to read a property from an instance of a class given the property name
 * throws exception if property not found
 * ### Example:
 * ```
 * sample.readInstanceProperty<Int>("age")
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

/*fun <D: Any> D.primaryConstructorPropertiesWithValues(): Map<KProperty1<D, *>, Any?> {
    val dataClass = this::class
    require(dataClass.isData) { "It is working only for data class" }
    val propertyNames = dataClass.primaryConstructor!!.parameters.map{ it.name }
    val properties = propertyNames.map { name ->
        dataClass.memberProperties.find { it.name == name }
    }
    val list = properties.map{it to it!!.get(this)}
    val result = buildMap<KProperty1<D,*>, Any?> {

    }
}*/
@Suppress("UNCHECKED_CAST")
inline fun <reified R : Any> getAllPropertiesOfType(instance: Any): List<KParameter> {
    val dataClass = instance::class
    require(dataClass.isData) { "instance must be a data class" }
    val properties = dataClass.primaryConstructor!!.parameters.filter{ it.type.classifier as KClass<R> == R::class}
    @Suppress("UNCHECKED_CAST")
    return properties
}
@Suppress("UNCHECKED_CAST")
inline fun <reified R: Any> getAllPropertyNamesOfType(instance: Any): List<String> {
    val dataClass = instance::class
    require(dataClass.isData) { "instance must be a data class" }
    val properties = dataClass.primaryConstructor!!.parameters.filter{ it.type.classifier as KClass<R> == R::class}

    return properties.map{ it.name!! }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified R : Any> getAllPropertiesOfType2(instance: Any): List<KProperty1<Any, *>> {
    val dataClass = instance::class
    require(dataClass.isData) { "instance must be a data class" }
    val properties = dataClass.memberProperties.filter{ it.returnType.classifier as KClass<R> == R::class}
    @Suppress("UNCHECKED_CAST")
    return properties.map{ it as KProperty1<Any, R> }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified R : Any, T: Any> T.copyAllWithNewValue(newValues: List<Any>): T {
    val dataClass = this::class
    require(dataClass.isData) { "instance must be a data class" }
    val properties = dataClass.memberProperties.filter{ it.returnType.classifier as KClass<R> == R::class}
    @Suppress("UNCHECKED_CAST")
    val propArray = properties.zip(newValues){p,v -> p to v }.toTypedArray()
    return this.copyDataObject( *propArray)
}

