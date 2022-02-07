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

abstract class Tower(hd: TowerHandler<out Tower>) {

    val service = hd.service
    val uid = hd.uid

    init {
        hd.onLoad { world ->
            if (!this@Tower::location.isInitialized) {
                val pos = getList("Pos").map { it as Double }
                val rotation = getList("Rotation").map { it as Float }
                location = Location(world, pos[0], pos[1], pos[2], rotation[0], rotation[1])
            }
            if (!this@Tower::location.isInitialized) {
                throw Error("no tower location: $uid")
            }

            if (containsKey("OwnerMost") && containsKey("OwnerLeast")) {
                val ownerMost = getLong("OwnerMost")
                val ownerLeast = getLong("OwnerLeast")
                ownerUid = if (ownerMost != 0L && ownerLeast != 0L ) UUID(ownerMost, ownerLeast) else null
            }

            if (containsKey("Groups")) {
                groups = HashSet(getList("Groups").map { it as String })
            }
        }
        hd.onSave {
            this["Pos"] = listOf(location.x, location.y, location.z)
            this["Rotation"] = listOf(location.yaw, location.pitch)

            this["OwnerMost"] = ownerUid?.mostSignificantBits ?: 0L
            this["OwnerLeast"] = ownerUid?.mostSignificantBits ?: 0L
            this["Groups"] = groups
            this["Type"] = this@Tower::class.jvmName
        }
        hd.onStop {
            dead = true
        }
    }

    companion object {
        @JvmStatic
        val plugin = Bukkit.getPluginManager().getPlugin("PowerTower") as PowerTowerPlugin
        @JvmStatic
        protected val rsp: RegisteredServiceProvider<Permission> = Bukkit.getServicesManager().getRegistration(Permission::class.java) as RegisteredServiceProvider<Permission>
        @JvmStatic
        val perms = rsp.provider // Vault
    }

    private var ownerUid: UUID? = null

    var owner by ownerAsUid(::ownerUid)
        private set

    fun isOwner(player: OfflinePlayer) = player.uniqueId == ownerUid

    fun isTeamMember(player: OfflinePlayer) = perms.getPlayerGroups(location.world?.name ?: throw Exception("world unloaded"), player).any { groups.contains(it) }

    var groups: Set<String> = setOf()
        private set

    lateinit var location: Location
        private set

    var dead: Boolean = false
        private set

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
}
