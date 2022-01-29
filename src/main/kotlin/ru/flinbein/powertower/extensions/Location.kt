package ru.flinbein.powertower.extensions

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

infix fun <T: Location> T.unto(b: Location) = Vector(b.x - this.x, b.y - this.y, b.z - this.z)
infix fun <T: Location> T.unto(b: Entity) = this unto b.location
infix fun <T: Location> T.unto(b: Block) = this unto b.location

operator fun <T: Location> T.component1() = this.x
operator fun <T: Location> T.component2() = this.y
operator fun <T: Location> T.component3() = this.z
operator fun <T: Location> T.component4() = this.world