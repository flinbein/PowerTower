package ru.flinbein.powertower.tower

import de.Ste3et_C0st.FurnitureLib.main.FurnitureLib
import de.Ste3et_C0st.FurnitureLib.main.FurnitureManager
import de.Ste3et_C0st.FurnitureLib.main.ObjectID
import me.dpohvar.powernbt.api.NBTCompound
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import ru.flinbein.powertower.service.TowerHandler


abstract class FurnitureTower(hd: TowerHandler<out FurnitureTower>): Tower(hd) {

    lateinit var furniture: ObjectID
        private set

    companion object {
        @JvmStatic
        protected val furnitureLib = Bukkit.getPluginManager().getPlugin("FurnitureLib") as FurnitureLib
        @JvmStatic
        protected val furnitureManager: FurnitureManager = furnitureLib.furnitureManager
    }

    fun containsBlock(loc: Location) = furniture.containsBlock(loc)

    fun getChunkCoordinates(): Set<Pair<Int, Int>> {
        val set = HashSet<Pair<Int, Int>>()
        for (location in furniture.blockList) {
            set.add((location.blockX shr 4) to (location.blockZ shr 4))
        }
        return set
    }

    abstract fun buildFurniture(): ObjectID

    init {
        hd.onLoad {
            var foundFurniture: ObjectID? = null
            val string = getString("FurnitureSerial")
            if (string.isNotEmpty()) foundFurniture = furnitureManager.getObjBySerial(string)
            furniture = foundFurniture ?: buildFurniture()
        }

        hd.onSave {
            this["FurnitureSerial"] = furniture.serial
        }

        hd.onDestroy {
            furniture.remove()
        }
    }
}
