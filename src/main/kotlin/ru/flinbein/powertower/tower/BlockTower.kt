package ru.flinbein.powertower.tower

import org.bukkit.Location
import ru.flinbein.powertower.service.TowerHandler

abstract class BlockTower(hd: TowerHandler<out Tower>): Tower(hd), BlocksHolder {

    abstract val blockCoordinates: Collection<Location>

    override fun containsBlock(loc: Location): Boolean {
        val x = loc.blockX
        val y = loc.blockY
        return blockCoordinates.any { it.blockX == x && it.blockY == y }
    }

    override fun getChunkCoordinates(): Set<Pair<Int, Int>> {
        val set: HashSet<Pair<Int, Int>> = HashSet()
        for (loc in blockCoordinates) {
            set.add((loc.blockX ushr 4) to (loc.blockY ushr 4))
        }
        return set
    }


}