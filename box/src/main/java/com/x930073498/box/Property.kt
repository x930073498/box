@file:Suppress("UNCHECKED_CAST")

package com.x930073498.box

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private object Property : BoxFactory<KProperty<*>, Any?>


@PublishedApi
internal fun <T : BoxProvider> T.propertyBox() = getBox(Property)


operator fun <T : BoxProvider, V> T.getValue(thisRef: T, kProperty: KProperty<*>): V? =
    propertyBox()[kProperty] as? V

operator fun <T : BoxProvider, V> T.setValue(thisRef: T, kProperty: KProperty<*>, value: V) {
    propertyBox()[kProperty] = value
}

fun <T : BoxProvider, V> property(): ReadWriteProperty<T, V?> = NullablePropertyDelegate()

fun <T : BoxProvider, V> property(defaultValue: T. () -> V): ReadWriteProperty<T, V> =
    DefaultValuePropertyDelegate(defaultValue)

fun <T : BoxProvider, V> property(defaultValue: V) = property<T, V> {
    defaultValue
}

private class NullablePropertyDelegate<T : BoxProvider, V> : ReadWriteProperty<T, V?> ,Subscribable{
    override fun getValue(thisRef: T, property: KProperty<*>): V? {
        return thisRef.getValue(thisRef, property)
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V?) {
        thisRef.setValue(thisRef, property, value)
    }

}

private class DefaultValuePropertyDelegate<T : BoxProvider, V>(private val defaultValue: T.() -> V) :
    ReadWriteProperty<T, V> ,Subscribable{
    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return thisRef.propertyBox().getOrCreate(property) {
            defaultValue(thisRef)
        }
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        thisRef.propertyBox()[property] = value
    }

}