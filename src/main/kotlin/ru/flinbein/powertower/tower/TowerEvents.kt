package ru.flinbein.powertower.tower

import me.dpohvar.powernbt.api.NBTCompound
import org.bukkit.World

interface TowerEvents {

    fun load(world: World, data: NBTCompound){}

    fun save(data: NBTCompound){}

    fun onStart() {}

    fun onDestroy() {}

}