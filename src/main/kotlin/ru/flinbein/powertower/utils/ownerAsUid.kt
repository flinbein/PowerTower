package ru.flinbein.powertower.utils

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

fun <T> ownerAsUid(prop: KMutableProperty0<UUID?>) = object: ReadWriteProperty<T, OfflinePlayer?> {
    override fun getValue(thisRef: T, property: KProperty<*>): OfflinePlayer? {
        return Bukkit.getOfflinePlayer(prop.get() ?: return null)
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: OfflinePlayer?) {
        prop.set(value?.uniqueId)
    }
}