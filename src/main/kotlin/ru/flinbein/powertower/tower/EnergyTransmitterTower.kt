package ru.flinbein.powertower.tower

import me.dpohvar.powernbt.api.NBTCompound
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.block.ShulkerBox
import ru.flinbein.powertower.extensions.*
import ru.flinbein.powertower.service.TaskManager
import ru.flinbein.powertower.service.TowerHandler

class EnergyTransmitterTower(private val hd: TowerHandler<out Tower>): BlockTower(hd), OrderEnergyTransmitter by hd(), EnergyReceiver {

    override val energyReceivePoint by lazy { blockCoordinates[1].add(0.5, 0.5, 0.5) }

    override val blockCoordinates: List<Location>
        get() = listOf(location.clone(), location.clone().add(.0,1.0,.0))

    override fun beforeTransmitAnimation(task: TaskManager): Long {
        val state = blockCoordinates[1].block.state as ShulkerBox
        state.open()
        return 5L
    }

    override fun transmitAnimation(transmitted: Double, receiver: EnergyReceiver, task: TaskManager): Long {
        val (x1, y1, z1, world) = energyReceivePoint
        val (x2, y2, z2) = receiver.energyReceivePoint
        val dx = x2-x1
        val dy = y2-y1
        val dz = z2-z1
        val distance = energyReceivePoint.distance(receiver.energyReceivePoint)
        var pos = 0.0
        while (pos < distance) {
            pos += 0.2
            val loc = Location(world, x1 + dx*pos/distance, y1 + dy*pos/distance, z1 + dz*pos/distance)
            world?.spawnParticle(Particle.REDSTONE, loc, 1, DustOptions(Color.FUCHSIA, 1f))
        }
        return 0L
    }

    override fun afterTransmitAnimation(transmitted: Double, receiver: EnergyReceiver?, task: TaskManager): Long {
        val state = blockCoordinates[1].block.state as ShulkerBox
        state.close()
        return 0L
    }

    init {
        hd.onLoad {
            blockCoordinates[0].block.blockData = Material.REDSTONE_BLOCK.createBlockData()
            blockCoordinates[1].block.blockData = Material.RED_SHULKER_BOX.createBlockData()
        }
    }
}