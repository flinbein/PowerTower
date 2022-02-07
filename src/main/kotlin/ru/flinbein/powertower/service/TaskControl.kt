package ru.flinbein.powertower.service

interface TaskControl {
    val isPending: Boolean
    fun stop()

    companion object {
        val Stopped: TaskControl = object : TaskControl {
            override fun stop() {}
            override val isPending = true
        }
    }
}