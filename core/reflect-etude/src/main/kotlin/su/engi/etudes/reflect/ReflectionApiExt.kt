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
 * val daughter = Person(Person("Jane", 7, Sex.FEMALE)
 * val grandfather = person.copyDataObject(
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

fun <R: Any, T: Any> T.copyDataInstanceWithUpdatedPropertiesOfType(type: KClass<R>, newValues: List<Any>){

}
/**
 * The values updater - returns instance with updated properties of the particular type with provided new values.
 * **Important:** no parameter type check for new values is applied, either not quantity of new values check
 * ### Example:
 * ```kotlin
 * val old = BirthBook (names = listOf("Ann", "Susan"),
 *                      years = listOf(1999, 2007),
 *                      approved = true
 *                      )
 * val new = old.updatePropertiesOfType<List<*>, BirthBook>(listOf("Andrey"), listOf(2000))
 * new.names[0] == "Andrey" // True
 * ```
 * @receiver [T] generic of the data class
 * @param [R] generic of particular type to find properties
 * @param [newValues] new values for [T] properties of type [R]
 * @return new instance of the same data class [T] *
 * @throws exception if receiver is not a data class
 */
inline fun <reified R : Any, T: Any> T.updatePropertiesOfType(vararg newValues: Any): T {

    val properties = this::class.memberProperties.filter{ it.returnType.classifier == R::class}

    val propArray = properties.zip(newValues){p, v ->
        /*val interest = v::class.members.first { it.name == p.name } as KProperty1<Any, *>
        require(p.returnType.arguments.map{it.type!!.classifier} == v::class.typeParameters.map{it.javaClass}){
            "type mismatch ${p.returnType.arguments.map{it.type!!.classifier}}  ${v::class.typeParameters.map{it.createType()}}"}*/
        p to v }.toTypedArray()
    return this.copyDataObject( *propArray)
}

fun <T: Any> T.updatePropertyOfName(name: String, newValue: Any): T {
    val property = this::class.memberProperties.first{ it.name == name }
    return this.copyDataObject( property to newValue)
}


/*inline fun <reified R: Any> KProperty0<R>.updateDataClassValue(name: String, newValue: Any): KProperty0<R> {
    @Suppress("UNCHECKED_CAST")
    val dataClass = this.returnType.classifier as KClass<R>
    require(dataClass.isData) { "instance must be a data class" }
    val outerInstance = this.instanceParameter
    val inst = this.get()
    val prop = dataClass.memberProperties.find{it.name == name}
    val newInstance = inst.updatePropertyOfName(name, newValue)

}*/

fun <T: Any> T.allPropertiesWithValuesAsString(): String {
    val dataClass = this::class
    require(dataClass.isData) { "instance must be a data class" }
    val properties = dataClass.primaryConstructor!!.parameters.map{it.name}
    val propsAndValues = properties.filterNotNull().map{ it to instancePropertyAsString(this, it)}
    return propsAndValues.joinToString(separator = ", ") { "${it.first}: ${it.second}" }
}

/*

when (val type = kClass.memberProperties.find {it.name == name}!!.returnType) {
    ValidOrFocusedAtCheck::class.createType(type.arguments) ->
    readInstanceProperty<ValidOrFocusedAtCheck<*>>(this, name)
    else -> throw IllegalArgumentException(
    "not applicable type of the field. Only ${ValidOrFocusedAtCheck::class.simpleName} supported"
    )
}*/
@Suppress("UNCHECKED_CAST")
fun instancePropertyAsString(instance: Any, propertyName: String): String {
    val property = instance::class.members
        .first { it.name == propertyName } as KProperty1<Any, *>
    return property.get(instance).toString()
}