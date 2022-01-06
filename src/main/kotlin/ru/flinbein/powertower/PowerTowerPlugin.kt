package ru.flinbein.powertower

import org.bukkit.plugin.java.JavaPlugin

class PowerTowerPlugin : JavaPlugin {

    constructor() : super() {
        logger.info("Constructor of plugin"); // not work actually. Logger has nowhere to log at this point
    }

    override fun onEnable() {
        super.onEnable();
        logger.info("PWRTWR ENABLED")
    }

}