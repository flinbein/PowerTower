package ru.flinbein.powertower.service

import me.dpohvar.powernbt.PowerNBT
import me.dpohvar.powernbt.api.NBTCompound
import org.bukkit.Bukkit
import org.bukkit.World
import ru.flinbein.powertower.PowerTowerPlugin
import ru.flinbein.powertower.tower.Tower
import ru.flinbein.powertower.tower.TowerEvents
import java.io.File
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmName

class PowerTowerService(private val plugin: PowerTowerPlugin) {
    private val nbt = PowerNBT.getApi()
    private val uuidRegex = Regex("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")

    private val hashTowersByClass: HashMap<KClass<*>, HashSet<Tower>> = HashMap()
    private val hashTowersByUid: HashMap<UUID, TowerHandler.TowerInit<out Tower>> = HashMap()

    private fun <T: Tower> putTowerToHash(towerInit: TowerHandler.TowerInit<out T>, tower: T){
        hashTowersByUid[towerInit.initializer.uid] = towerInit
        val towerClassesAndInterfaces = ArrayList<KClass<*>>()
        towerClassesAndInterfaces.add(towerInit::class)
        towerClassesAndInterfaces.addAll(tower::class.allSuperclasses)
        for (towerCi in towerClassesAndInterfaces) {
            val towerSet = hashTowersByClass.getOrPut(towerCi) { HashSet() }
            towerSet.add(tower)
        }
    }

    private fun removeTowerFromHash(uid: UUID){
        val towerInit = hashTowersByUid[uid] ?: return
        val tower = towerInit.initializer.tower
        hashTowersByUid.remove(uid)
        val towerClassesAndInterfaces = ArrayList<KClass<*>>()
        towerClassesAndInterfaces.add(towerInit::class)
        towerClassesAndInterfaces.addAll(tower::class.allSuperclasses)
        for (towerCi in towerClassesAndInterfaces) {
            val towerSet = hashTowersByClass[towerCi] ?: continue
            towerSet.remove(tower)
            if (towerSet.isEmpty()) hashTowersByClass.remove(towerCi)
        }
    }

    fun <T: Tower> createTower(type: KClass<T>, world: World, data: NBTCompound): T{
        return createTower(type, world, UUID.randomUUID(), data)
    }

    private fun <T: Tower> createTower(type: KClass<T>, world: World, uid: UUID, data: NBTCompound): T {
        val towerInit = TowerHandler.TowerInit<T>(this, uid, plugin)
        try {
            val tower = type.constructors.first().call(towerInit.initializer)
            putTowerToHash(towerInit, tower)
            towerInit.initTower(tower)
            tower.load(world, data)
            for (controller in towerInit.controllers.values) {
                if (controller is TowerEvents) controller.load(world, data)
            }
            return tower
        } catch (error: Throwable) {
            removeTowerFromHash(uid)
            towerInit.destroy()
            throw error
        }
    }

    fun loadTowers(world: World){
        val worldUid = world.uid
        val towersFolder = File(plugin.dataFolder, "towers")
        val worldFolder = File(towersFolder, worldUid.toString())
        if (!worldFolder.isDirectory) return
        val files = worldFolder.listFiles() ?: return
        val towersData = HashMap<UUID, NBTCompound>()
        for (file in files) {
            if (!file.isFile) continue
            if (file.extension != "nbtz") continue
            if (!file.nameWithoutExtension.matches(uuidRegex)) continue
            towersData[UUID.fromString(file.nameWithoutExtension)] = nbt.readCompressed(file) as NBTCompound
        }
        loadTowers(world, towersData)
    }

    private fun loadTowers(world: World, towersData: Map<UUID, NBTCompound>){
        for ((uid, data) in towersData) {
            val towerTypeName = data.getString("Type")

            val towerInit = hashTowersByUid[uid]
            val existingTower = towerInit?.initializer?.tower
            if (existingTower != null) {
                val jvmName = existingTower::class.jvmName
                if (jvmName != towerTypeName) {
                    plugin.logger.warning("Tower $uid type mismatch: $jvmName stored as $towerTypeName")
                    continue
                }
                try {
                    existingTower.load(world, data)
                    for (controller in towerInit.controllers.values) {
                        if (controller is TowerEvents) controller.load(world, data)
                    }
                } catch (error: Throwable) {
                    plugin.logger.warning("Tower $uid $towerTypeName can not load data from file")
                    plugin.logger.throwing(PowerTowerService::class.jvmName, "loadTowers", error)
                }
                continue
            }

            val towerClass = getTowerClass(towerTypeName)
            if (towerClass == null) {
                plugin.logger.warning("Tower $uid unknown type: $towerTypeName")
                continue
            }
            try {
                createTower(towerClass, world, uid, data)
            } catch (error: Throwable) {
                plugin.logger.warning("Tower $uid can not create tower of type $towerTypeName")
                plugin.logger.throwing(PowerTowerService::class.jvmName, "loadTowers", error)
            }

        }
    }

    fun saveTowers(){
        val towersFolder = File(plugin.dataFolder, "towers")
        for ((uid, towerInit) in hashTowersByUid) {
            val tower = towerInit.initializer.tower
            val world = tower.location.world ?: continue
            val worldFolder = File(towersFolder, world.uid.toString())
            worldFolder.mkdirs()
            val data = NBTCompound()
            try {
                tower.save(data)
                for (controller in towerInit.controllers.values) {
                    if (controller is TowerEvents) controller.save(data)
                }
            } catch (error: Throwable) {
                plugin.logger.warning("Tower $uid: can not save to NBT")
                plugin.logger.throwing(PowerTowerService::class.jvmName, "saveTowers", error)
                continue
            }
            data["Type"] = tower::class.jvmName

            try {
                val towerFile = File(worldFolder, "$uid.nbtz")
                nbt.writeCompressed(towerFile, data)
            } catch (error: Throwable) {
                plugin.logger.warning("Tower $uid: can not save to file")
                plugin.logger.throwing(PowerTowerService::class.jvmName, "saveTowers", error)
                continue
            }
        }
    }

    fun destroyTower(uid: UUID){
        val towerInit = hashTowersByUid[uid]
        removeTowerFromHash(uid)
        val tower = towerInit?.initializer?.tower
        tower?.onDestroy()
        val controllers = towerInit?.controllers?.values
        towerInit?.destroy()
        if (controllers != null) for (controller in controllers) {
            if (controller is TowerEvents) controller.onDestroy()
        }
        if (tower != null) run {
            val world = tower.location.world ?: return@run
            val towersFolder = File(plugin.dataFolder, "towers")
            val worldFolder = File(towersFolder, world.uid.toString() )
            val towerFile = File(worldFolder, "$uid.nbtz")
            if (towerFile.exists()) towerFile.delete()
        }
    }

    inline operator fun <reified T> get(uid: UUID): T? {
        val tower = getTower(uid)
        return if (tower is T) tower else null
    }

    private fun getTowerClass(name: String): KClass<out Tower>? {
        for (plugin in Bukkit.getPluginManager().plugins) {
            try {
                val kClass = plugin::class.java.classLoader.loadClass(name).kotlin
                if (!kClass.isSuperclassOf(Tower::class)) continue
                return kClass as KClass<out Tower>
            } catch (ignored: Throwable) {}
        }
        return null
    }

    fun getTower(uid: UUID) = hashTowersByUid[uid]?.initializer?.tower



}