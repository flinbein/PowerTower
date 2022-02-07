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

    class EnergyHolderController(hd: TowerHandler<Tower>): EnergyHolder {

        override var maxEnergy: Double = .0
        override var energy: Double = .0

        init {
            hd.onLoad {
                if (containsKey("Energy")) energy = getDouble("Energy")
                if (containsKey("MaxEnergy")) maxEnergy = getDouble("MaxEnergy")
            }
            hd.onSave {
                this["Energy"] = energy
                this["MaxEnergy"] = maxEnergy
            }
        }
    }
}

