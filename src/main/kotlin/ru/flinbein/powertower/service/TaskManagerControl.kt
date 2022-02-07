package ru.flinbein.powertower.service

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import ru.flinbein.powertower.PowerTowerPlugin
import java.util.HashMap

interface TaskManagerControl: TaskManager, TaskControl {

    companion object {
        private val scheduler = Bukkit.getScheduler()
    }

    class Instance(private val plugin: PowerTowerPlugin, parent: TaskManager): TaskManagerControl{
        override var isPending = true
            private set

        override var isDestroyed = false
            private set

        var isInitialized = false
            private set


        private val bukkitTasks = HashMap<TaskControl, BukkitTask>()
        private val onStopTasks = HashMap<TaskControl, () -> Unit>()
        private val onImmediateTasks = HashMap<TaskControl, () -> Unit>()
        private val onDestroyTasks = HashMap<TaskControl, () -> Unit>()

        init {
            parent.onImmediate {
                isInitialized = true
                val tasks = ArrayList(onImmediateTasks.values)
                onImmediateTasks.clear()
                for (task in tasks) task()
            }
            parent.onStop(this@Instance::stop)
            parent.onDestroy {
                isDestroyed = true
                val tasks = ArrayList(onDestroyTasks.values)
                onDestroyTasks.clear()
                for (task in tasks) task()
            }
        }

        override fun onTimeout(delay: Long, task: () -> Unit): TaskControl {
            if (!isPending) return TaskControl.Stopped
            var bukkitTask: BukkitTask? = null
            val taskControl = object : TaskControl {
                override val isPending: Boolean
                    get() = !(bukkitTask?.isCancelled ?: true)

                override fun stop() {
                    bukkitTasks.remove(this)
                    bukkitTask?.cancel()
                }
            }
            bukkitTask = scheduler.runTaskLater(plugin, { taskControl.stop(); task() } as Runnable, delay)
            if (!bukkitTask.isCancelled) bukkitTasks[taskControl] = bukkitTask
            return taskControl
        }

        override fun onTimer(delay: Long, period: Long, task: (ctrl: TaskControl) -> Unit): TaskControl {
            if (!isPending) return TaskControl.Stopped
            var bukkitTask: BukkitTask? = null
            val taskControl = object : TaskControl {
                override val isPending: Boolean
                    get() = !(bukkitTask?.isCancelled ?: true)

                override fun stop() {
                    bukkitTasks.remove(this)
                    bukkitTask?.cancel()
                }
            }
            bukkitTask = scheduler.runTaskTimer(plugin, { task(taskControl) } as Runnable, delay, period)
            if (!bukkitTask.isCancelled) bukkitTasks[taskControl] = bukkitTask
            return taskControl
        }

        override fun onStop(task: () -> Unit): TaskControl {
            if (!isPending) {
                task()
                return TaskControl.Stopped
            }
            val taskControl = object: TaskControl {
                override val isPending by this@Instance::isPending

                override fun stop(){
                    onStopTasks.remove(this)
                }
            }
            onStopTasks[taskControl] = task
            return taskControl
        }

        override fun onDestroy(task: () -> Unit): TaskControl {
            if (isDestroyed) {
                task()
                return TaskControl.Stopped
            }
            val taskControl = object: TaskControl {
                override val isPending by this@Instance::isPending

                override fun stop(){
                    onDestroyTasks.remove(this)
                }
            }
            onDestroyTasks[taskControl] = task
            return taskControl
        }

        override fun onImmediate(task: () -> Unit): TaskControl {
            if (isInitialized) {
                scheduler.runTask(plugin, task)
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

        override fun taskGroup(): TaskManagerControl {
            if (!isPending) return this
            return Instance(plugin, this)
        }

        override fun stop() {
            isPending = false
            val tasksOnStop = onStopTasks.values.toList()
            onStopTasks.clear()
            tasksOnStop.forEach { it() }

            val bTasks = ArrayList(bukkitTasks.keys)
            bukkitTasks.clear()
            for (task in bTasks) task.stop()

            val imTasks = ArrayList(onImmediateTasks.keys)
            onImmediateTasks.clear()
            for (imTask in imTasks) imTask.stop()
        }
    }
}