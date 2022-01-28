package ru.flinbein.powertower.tower

import me.dpohvar.powernbt.api.NBTCompound
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.plugin.RegisteredServiceProvider
import ru.flinbein.powertower.PowerTowerPlugin
import ru.flinbein.powertower.service.TowerHandler
import ru.flinbein.powertower.utils.ownerAsUid
import java.util.*
import kotlin.reflect.jvm.jvmName

abstract class Tower(hd: TowerHandler<out Tower>): TowerEvents {

    val service = hd.service
    val uid = hd.uid

    companion object {
        @JvmStatic
        val plugin = Bukkit.getPluginManager().getPlugin("PowerTower") as PowerTowerPlugin
        @JvmStatic
        protected val rsp: RegisteredServiceProvider<Permission> = Bukkit.getServicesManager().getRegistration(Permission::class.java) as RegisteredServiceProvider<Permission>
        @JvmStatic
        val perms = rsp.provider
    }

    private var ownerUid: UUID? = null

    var owner by ownerAsUid(::ownerUid)
        private set

    fun isOwner(player: OfflinePlayer) = player.uniqueId == ownerUid

    fun isTeamMember(player: OfflinePlayer) = perms.getPlayerGroups(location.world?.name ?: throw Exception("world unloaded"), player).any { groups.contains(it) }

    lateinit var groups: Set<String>
        private set

    lateinit var location: Location
        private set

    var dead: Boolean = false
        private set

    override fun onDestroy() {
        dead = true
    }

    fun addGroup(group: String){
        this.groups = sequence {
            yieldAll(groups)
            yield(group)
        }.toSet()
    }

    fun removeGroup(group: String): Boolean {
        val hashSet = HashSet(groups)
        val result = hashSet.remove(group)
        groups = hashSet
        return result
    }

    override fun load(world: World, data: NBTCompound){
        if (!this::location.isInitialized) {
            val pos = data.getList("Pos").map { it as Double }
            val rotation = data.getList("Rotation").map { it as Float }
            location = Location(world, pos[0], pos[1], pos[2], rotation[0], rotation[1])
        }

        if (data.containsKey("OwnerMost") && data.containsKey("OwnerLeast")) {
            val ownerMost = data.getLong("OwnerMost")
            val ownerLeast = data.getLong("OwnerLeast")
            ownerUid = if (ownerMost != 0L && ownerLeast != 0L ) UUID(ownerMost, ownerLeast) else null
        }

        if (data.containsKey("Groups")) {
            groups = HashSet(data.getList("Groups").map { it as String })
        }
    }

    override fun save(data: NBTCompound) {
        data["Pos"] = listOf(location.x, location.y, location.z)
        data["Rotation"] = listOf(location.yaw, location.pitch)

        data["OwnerMost"] = ownerUid?.mostSignificantBits ?: 0L
        data["OwnerLeast"] = ownerUid?.mostSignificantBits ?: 0L
        data["Groups"] = groups
        data["Type"] = this::class.jvmName
    }
}
