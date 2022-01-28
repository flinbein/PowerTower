package ru.flinbein.powertower

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.plugin.java.JavaPlugin
import ru.flinbein.powertower.service.PowerTowerService

class PowerTowerPlugin : JavaPlugin(), Listener {

    val towerService = PowerTowerService(this)

    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(this, this)
        server.worlds.forEach (towerService::loadTowers)
    }

    override fun onDisable() {
        towerService.saveTowers()
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent){
        towerService.loadTowers(event.world)
    }

    @EventHandler
    fun onWorldUnload(event: WorldUnloadEvent){
        towerService.saveTowers()
    }


}