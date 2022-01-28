package ru.flinbein.powertower.service

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import ru.flinbein.powertower.PowerTowerPlugin
import ru.flinbein.powertower.annotations.TowerControl
import ru.flinbein.powertower.tower.Tower
import java.util.*
import kotlin.reflect.KClass

class TowerHandler<T: Tower>(val service: PowerTowerService, val uid: UUID, val plugin: PowerTowerPlugin): TaskManager {
    private val scheduler = Bukkit.getScheduler()
    private val tasks = HashSet<BukkitTask>()
    private val immediateTasks = HashMap<TaskControl, () -> Unit>()

    private var initialized: Boolean = false
        private set

    lateinit var tower: T
        private set

    override var isPending = true
        private set

    private val controllers = HashMap<KClass<*>, Any>()

    fun <K: Any> registerController(clazz: KClass<K>): K {
        if (this.controllers.contains(clazz)) return controllers[clazz] as K
        if (!clazz.java.isInterface) throw Exception("Interface expected: "+clazz.qualifiedName)
        val annotation = clazz.annotations.find { it is TowerControl } ?: throw Exception("Interface has no @TowerControl: "+clazz.qualifiedName)
        val controllerClass = (annotation as TowerControl).controllerClass
        if (controllerClass.isAbstract) throw Exception("Controller class is abstract: "+clazz.qualifiedName)
        if (!controllerClass.isInstance(clazz)) throw Exception("Controller must implement interface: "+clazz.qualifiedName)
        val controller = controllerClass.constructors.first().call(this) as K
        controllers[clazz] = controller
        return controller
    }

    override fun onTimeout(delay: Long, task: () -> Unit): TaskControl.BukkitTaskControl {
        var bukkitTask: BukkitTask? = null
        bukkitTask = scheduler.runTaskLater(plugin, { tasks.remove(bukkitTask); bukkitTask?.cancel(); task() } as Runnable, delay)
        if (!bukkitTask.isCancelled) tasks.add(bukkitTask)
        return TaskControl.BukkitTaskControl(bukkitTask)
    }

    override fun onTimer(delay: Long, period: Long, task: (ctrl: TaskControl) -> Unit): TaskControl.BukkitTaskControl {
        var bukkitTaskControl: TaskControl? = null
        val bukkitTask = scheduler.runTaskTimer(plugin, { task(bukkitTaskControl as TaskControl) } as Runnable, delay, period)
        bukkitTaskControl = TaskControl.BukkitTaskControl(bukkitTask)
        if (!bukkitTask.isCancelled) tasks.add(bukkitTask)
        return bukkitTaskControl
    }

    override infix fun onImmediate(task: () -> Unit): TaskControl {
        if (initialized) {
            scheduler.runTask(plugin, task)
            return TaskControl.stopped
        }
        val taskControl = object: TaskControl {
            override val isPending: Boolean
                get() = !initialized

            override fun stop(){
                immediateTasks.remove(this)
            }
        }
        immediateTasks[taskControl] = task
        return taskControl
    }

    override fun onStop(task: () -> Unit): TaskControl {
        TODO("Not yet implemented")
    }

    override fun taskGroup(task: () -> Unit): TaskManagerControl {
        TODO("Not yet implemented")
    }

    inline operator fun <reified K: Any> invoke(): K {
        return this.registerController(K::class)
    }

    class TowerInit<T: Tower>(service: PowerTowerService, uid: UUID, plugin: PowerTowerPlugin){
        val initializer = TowerHandler<T>(service, uid, plugin)
        val controllers by initializer::controllers

        fun initTower(tower: T) {
            initializer.tower = tower
            initializer.initialized = true
            val immediateTasks = initializer.immediateTasks.values.toList()
            initializer.immediateTasks.clear()
            immediateTasks.forEach { it() }
        }

        fun destroy() {
            initializer.isPending = false
            initializer.tasks.forEach { it.cancel() }
        }
    }
}