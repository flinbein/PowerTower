package ru.flinbein.powertower.utils

import org.bukkit.Location
import org.bukkit.Server
import java.util.*

data class TLocation(val x: Double, val y:  Double, val z: Double, val yaw: Float, val Pitch: Float, val uid: UUID) {

    constructor(l: Location) : this(l.x, l.y, l.z, l.yaw, l.pitch, l.world?.uid ?:throw RuntimeException("world unloaded"))

    fun getLocation(server: Server): Location? {
        val world = server.getWorld(uid) ?: return null
        return Location(world, x, y, z)
    }

    fun getWorld(server: Server) = server.getWorld(uid)

}
