package ru.flinbein.powertower.tower

import org.bukkit.Location

interface EnergyReceiver: EnergyHolder {

    val energyReceivePoint: Location

    fun onReceiveEnergy(value: Double, source: Any){}

    fun sendEnergy(value: Double, source: Any): Double {
        if (maxEnergy >= energy) return .0

        if (maxEnergy - energy > value) {
            energy += value
            onReceiveEnergy(value, source)
            return value
        }

        val diff: Double = maxEnergy - energy;
        energy = maxEnergy
        onReceiveEnergy(diff, source)
        return diff
    }
}



