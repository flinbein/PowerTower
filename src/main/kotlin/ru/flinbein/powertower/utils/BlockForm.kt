package ru.flinbein.powertower.utils

import org.bukkit.Location

class BlockForm(private vararg val blockPositions: Triple<Int, Int, Int>) {

    fun getBlocks(loc: Location) = blockPositions.map {
        loc.add(it.first.toDouble(), it.second.toDouble(), it.third.toDouble()).block
    }

    fun isLoaded(loc: Location) = blockPositions.all {
        loc.world?.isChunkLoaded((loc.blockX + it.first) ushr 4, (loc.blockZ + it.third) ushr 4) ?: false
    }


}
