package ru.flinbein.powertower.tower

import org.bukkit.Location
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent

interface BlocksHolder {

    fun containsBlock(loc: Location): Boolean

    fun getChunkCoordinates(): Set<Pair<Int, Int>>

    fun onInteract(event: PlayerInteractEvent){}

    fun onBreak(event: BlockBreakEvent){}

}