package ru.flinbein.powertower.tower

interface EnergyTransmitter: EnergyHolder {

    fun transmitEnergy(value: Double, dest: EnergyReceiver): Double {
        val transmitValue = if (value > energy) energy else value
        val transmitted = dest.sendEnergy(transmitValue, this)
        if (transmitted == .0) return .0
        energy -= transmitted
        return transmitted
    }
}



