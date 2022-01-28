package ru.flinbein.powertower.service

import org.bukkit.scheduler.BukkitTask

interface TaskControl {
    val isPending: Boolean
    fun stop()

    object stopped : TaskControl {
        override fun stop() {}
        override val isPending = true
    }

    class BukkitTaskControl(private val task: BukkitTask) : TaskControl {
        override val isPending
            get() = !task.isCancelled
        override fun stop() = task.cancel()
    }
}