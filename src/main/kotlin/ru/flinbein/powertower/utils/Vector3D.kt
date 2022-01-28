package ru.flinbein.powertower.utils

import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3D(val x: Double, val y:  Double, val z: Double) {

    constructor(v: Vector): this(v.x, v.y, v.z)

    val lengthSquared by lazy { x*x + y*y + z*z }
    val length by lazy { sqrt(lengthSquared) }

    operator fun plus(v: Vector3D) = Vector3D(v.x + x, v.y + y, v.z + z)
    operator fun plus(v: Vector) = Vector3D(v.x + x, v.y + y, v.z + z)

    operator fun minus(v: Vector3D) = Vector3D(v.x - x, v.y - y, v.z - z)
    operator fun minus(v: Vector) = Vector3D(v.x - x, v.y - y, v.z - z)

    operator fun times(v: Double) = Vector3D(x * v, y * v, z * v)

    operator fun div(v: Double) = Vector3D(x / v, y / v, z / v)

    operator fun rem(v: Double): Vector3D {
        if (lengthSquared == 0.0) return this
        val time = length * v
        return Vector3D(x * time, y * time, z * time)
    }

    operator fun rangeTo(v: Vector3D) = sqrt((x - v.x).pow(2) + (y-v.y).pow(2) + (z-v.z).pow(2))

    fun mid(vx: Double, vy: Double, vz: Double) = Vector3D((x + vx) / 2, (y + vy) / 2, (z + vz) / 2)
    infix fun mid(v: Vector3D) = mid(v.x, v.y, v.z)
    infix fun mid(v: Vector) = mid(v.x, v.y, v.z)

    infix fun rotX(angle: Double): Vector3D {
        val angleCos = cos(angle)
        val angleSin = sin(angle)
        return Vector3D(x, angleCos * y - angleSin * z, angleSin * y + angleCos * z)
    }

    infix fun rotY(angle: Double): Vector3D {
        val angleCos = cos(angle)
        val angleSin = sin(angle)
        return Vector3D(angleCos * x + angleSin * z, y, -angleSin * x + angleCos * z)
    }

    infix fun rotZ(angle: Double): Vector3D {
        val angleCos = cos(angle)
        val angleSin = sin(angle)
        return Vector3D(angleCos * x - angleSin * y, angleSin * x + angleCos * y, z)
    }

    fun toVector() = Vector(x, y, z)

    fun compareTo(v: Vector3D) = lengthSquared - v.lengthSquared

}
