package ru.flinbein.powertower.tower.aimable

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import ru.flinbein.powertower.annotations.TowerControl
import ru.flinbein.powertower.extensions.unto
import ru.flinbein.powertower.service.TowerHandler
import ru.flinbein.powertower.tower.Tower

@TowerControl(Aimable.Controller::class)
interface Aimable: AimSource {

    var target: Entity?
    val aimDistance: Double
    val aimFrom: Location

    enum class TargetPriority {
        RANDOM, WEAKEST, STRONGEST, NEAREST
    }

    enum class TargetType {
        OWNER, NOT_OWNER, PLAYER_NOT_OWNER, TEAMMATE, NOT_TEAMMATE, PLAYER_NOT_TEAMMATE, PLAYER, MOB, ALL
    }

    val priority: TargetPriority
    val targetType: TargetType


    class Controller<T: Tower>(private val hd: TowerHandler<T>): Aimable, AimSource by hd() {

        init {
            hd.onImmediate {
                val loc = hd.tower.location
                val world = loc.world as World
                hd.onTimer(0, 20) {
                    world.getNearbyEntities(loc, aimDistance, aimDistance, aimDistance, fun(entity: Entity): Boolean {
                        if (entity !is LivingEntity) return false
                        if (loc.distance(entity.location) < aimDistance) return false

                        return true;
                    })
                }
            }
        }

        private fun asdd(){
            val loc = hd.tower.location
            val world = loc.world as World
            world.getNearbyEntities(loc, aimDistance, aimDistance, aimDistance, fun(entity: Entity): Boolean {
                if (entity !is LivingEntity) return false
                if (loc.distance(entity.location) < aimDistance) return false

                return true;
            })
        }

        private fun getEntityFilter(): (Entity) -> Boolean = when (targetType) {
            TargetType.PLAYER -> ({ it is Player })
            TargetType.OWNER -> ({ it is Player && hd.tower.isOwner(it) })
            TargetType.NOT_OWNER -> ({ it !is Player || !hd.tower.isOwner(it) })
            TargetType.PLAYER_NOT_OWNER -> ({ it is Player && !hd.tower.isOwner(it) })
            TargetType.TEAMMATE -> ({ it is Player && hd.tower.isTeamMember(it) })
            TargetType.NOT_TEAMMATE -> ({ it !is Player || !hd.tower.isTeamMember(it) })
            TargetType.PLAYER_NOT_TEAMMATE -> ({ it is Player && !hd.tower.isTeamMember(it) })
            TargetType.MOB -> ({ it !is Player })
            TargetType.ALL -> ({ true })
        }

        override val aimFrom by hd.tower::location
        override val aimDistance: Double = 10.0

        override var target: Entity? = null
            set(value) {
                field = value
                if (value != null) aimToEntity(value)
            }

        private val towerAimSource by lazy { hd.tower as AimSource }

        fun aimToEntity(targetEntity: Entity){
            hd.onTimer(1, 1) {
                val locToTarget = aimSource.clone()
                locToTarget.direction = aimSource unto targetEntity
                towerAimSource.aimSource = locToTarget
            }
        }

        override val priority: TargetPriority = TargetPriority.NEAREST
        override val targetType: TargetType = TargetType.NOT_TEAMMATE
    }
}

