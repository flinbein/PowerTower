package ru.flinbein.powertower.tower

import me.dpohvar.powernbt.api.NBTCompound
import org.bukkit.World
import ru.flinbein.powertower.annotations.TowerControl
import ru.flinbein.powertower.service.TowerHandler

@TowerControl(EnergyHolder.EnergyHolderController::class)
interface EnergyHolder {

    var maxEnergy: Double
    var energy: Double

    val fullPowered: Boolean
            get() = energy >= maxEnergy

    class EnergyHolderController(ignored: TowerHandler<Tower>): EnergyHolder, TowerEvents {

        override var maxEnergy: Double = .0
        override var energy: Double = .0

        override fun load(world: World, data: NBTCompound) {
            if (data.containsKey("Energy")) energy = data.getDouble("Energy")
            if (data.containsKey("MaxEnergy")) maxEnergy = data.getDouble("MaxEnergy")
        }

        override fun save(data: NBTCompound) {
            data["Energy"] = energy
            data["MaxEnergy"] = maxEnergy
        }
    }
}

