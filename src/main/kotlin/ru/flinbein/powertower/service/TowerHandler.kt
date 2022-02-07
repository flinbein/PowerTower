package ru.flinbein.powertower.service

import me.dpohvar.powernbt.api.NBTCompound
import org.bukkit.Bukkit
import org.bukkit.World
import ru.flinbein.powertower.PowerTowerPlugin
import ru.flinbein.powertower.annotations.TowerControl
import ru.flinbein.powertower.tower.Tower
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class TowerHandler<T: Tower>(val service: PowerTowerService, val uid: UUID, val plugin: PowerTowerPlugin): TaskManager {
    private val onLoadTasks = HashMap<TaskControl, NBTCompound.(world: World) -> Unit>()
    private val onSaveTasks = HashMap<TaskControl, NBTCompound.() -> Unit>()

    private val onDestroyTasks = HashMap<TaskControl, () -> Unit>()
    private val onImmediateTasks = HashMap<TaskControl, () -> Unit>()
    private val onStopTasks = HashMap<TaskControl, () -> Unit>()
    private var isInitialized: Boolean = false



    lateinit var tower: T
        private set

    private val taskManager = TaskManagerControl.Instance(plugin, this)
    // Delegate methods?
    override var isPending = true
        private set
    override var isDestroyed = false
        private set
    override fun onTimeout(delay: Long, task: () -> Unit) = taskManager.onTimeout(delay, task)
    override fun onTimer(delay: Long, period: Long, task: (ctrl: TaskControl) -> Unit): TaskControl = taskManager.onTimer(delay, period, task)
    override fun taskGroup(): TaskManagerControl = taskManager.taskGroup()

    override fun onStop(task: () -> Unit): TaskControl {
        if (!isPending) {
            task()
            return TaskControl.Stopped
        }
        val taskControl = object: TaskControl {
            override val isPending by this@TowerHandler::isPending

            override fun stop(){
                onStopTasks.remove(this)
            }
        }
        onStopTasks[taskControl] = task
        return taskControl
    }

    override fun onImmediate(task: () -> Unit): TaskControl {
        if (isInitialized) {
            Bukkit.getScheduler().runTask(plugin, task)
            return TaskControl.Stopped
        }
        val taskControl = object: TaskControl {
            override val isPending: Boolean
                get() = !isInitialized

            override fun stop(){
                onImmediateTasks.remove(this)
            }
        }
        onImmediateTasks[taskControl] = task
        return taskControl
    }

    override fun onDestroy(task: () -> Unit): TaskControl {
        if (isDestroyed) {
            task()
            return TaskControl.Stopped
        }
        val taskControl = object: TaskControl {
            override val isPending: Boolean
                get() = !isInitialized

            override fun stop(){
                onDestroyTasks.remove(this)
            }
        }
        onDestroyTasks[taskControl] = task
        return taskControl
    }

    private val controllers: HashMap<KClass<*>, Any> = HashMap()

    fun <K: Any> registerController(clazz: KClass<K>): K {
        if (this.controllers.contains(clazz)) return controllers[clazz] as K
        if (!clazz.java.isInterface) throw Exception("Interface expected: "+clazz.qualifiedName)
        val annotation = clazz.annotations.find { it is TowerControl } ?: throw Exception("Interface has no @TowerControl: "+clazz.qualifiedName)
        val controllerClass = (annotation as TowerControl).controllerClass
        if (controllerClass.isAbstract) throw Exception("Controller class is abstract: "+clazz.qualifiedName)
        if (!controllerClass.isSubclassOf(clazz)) throw Exception("Controller must implement interface: "+clazz.qualifiedName)
        val controller = controllerClass.constructors.first().call(this) as K
        controllers[clazz] = controller
        return controller
    }

    fun onLoad(task: NBTCompound.(world: World) -> Unit): TaskControl {
        val taskControl = object: TaskControl {
            override val isPending by this@TowerHandler::isPending

            override fun stop(){
                onLoadTasks.remove(this)
            }
        }
        onLoadTasks[taskControl] = task
        return taskControl
    }

    fun onSave(task: NBTCompound.() -> Unit): TaskControl {
        val taskControl = object: TaskControl {
            override val isPending by this@TowerHandler::isPending

            override fun stop(){
                onSaveTasks.remove(this)
            }
        }
        onSaveTasks[taskControl] = task
        return taskControl
    }

    inline operator fun <reified K: Any> invoke(): K {
        return this.registerController(K::class)
    }

    class TowerInit<T: Tower>(service: PowerTowerService, uid: UUID, plugin: PowerTowerPlugin){
        val handler = TowerHandler<T>(service, uid, plugin)

        fun load(world: World, data: NBTCompound){
            handler.onLoadTasks.values.forEach { it(data, world) }
        }

        fun save(data: NBTCompound){
            handler.onSaveTasks.values.forEach { it(data) }
        }

        fun initTower(tower: T) {
            handler.tower = tower
            handler.isInitialized = true
            val immediateTasks = handler.onImmediateTasks.values.toList()
            handler.onImmediateTasks.clear()
            immediateTasks.forEach { it() }
        }

        fun stop(){
            if (!handler.isPending) return
            handler.isPending = false
            val immediateTasks = handler.onStopTasks.values.toList()
            handler.onStopTasks.clear()
            handler.onLoadTasks.clear()
            handler.onSaveTasks.clear()
            immediateTasks.forEach { it() }
        }

        fun destroy() {
            if (!handler.isPending) return
            handler.isDestroyed = true
            val dTasks = handler.onDestroyTasks.values.toList()
            dTasks.forEach { it() }
            stop()
        }
    }
}