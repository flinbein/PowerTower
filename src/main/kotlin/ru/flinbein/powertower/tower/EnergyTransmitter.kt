package ru.flinbein.powertower.tower

interface EnergyTransmitter: EnergyHolder {

    fun transmitEnergy(value: Double, dest: EnergyReceiver): Boolean {
        val transmitValue = if (value > energy) energy else value
        val transmitted = dest.sendEnergy(transmitValue, this)
        if (transmitted == .0) return false
        energy -= transmitted
        return true
    }
}



