package ru.flinbein.powertower.tower

import me.dpohvar.powernbt.api.NBTCompound
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import ru.flinbein.powertower.service.TowerHandler

class EnergyTransmitterTower(private val hd: TowerHandler<out Tower>): BlockTower(hd), EnergyHolder by hd(), EnergyTransmitter, EnergyReceiver {

    override val energyReceivePoint by lazy { blockCoordinates[1].add(0.5, 0.5, 0.5) }

    private var lastActionTime: Long = 0

    override val blockCoordinates: List<Location>
        get() = listOf(location.clone(), location.clone().add(.0,.1,.0))


    override fun onStart() {
        super.onStart()
    }

    override fun load(world: World, data: NBTCompound) {
        super.load(world, data)
        blockCoordinates[0].block.blockData = Material.REDSTONE_BLOCK.createBlockData()
        blockCoordinates[1].block.blockData = Material.RED_SHULKER_BOX.createBlockData()
    }

    override fun save(data: NBTCompound) {
        super.save(data)
    }


}