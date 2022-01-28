package ru.flinbein.powertower.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <K,V> runAfterChange(initValue: V, vararg runAfter: () -> Unit) = object: ReadWriteProperty<K,V> {
    var storedValue: V = initValue

    override fun getValue(thisRef: K, property: KProperty<*>): V = storedValue

    override fun setValue(thisRef: K, property: KProperty<*>, value: V) {
        if (storedValue == value) return
        storedValue = value
        runAfter.forEach { it() }
    }
}