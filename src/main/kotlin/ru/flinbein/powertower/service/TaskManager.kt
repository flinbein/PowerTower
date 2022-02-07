package ru.flinbein.powertower.service

interface TaskManager {

    val isPending: Boolean
    val isDestroyed: Boolean

    fun onTimeout(delay: Long = 0, task: () -> Unit): TaskControl
    fun onTimer(delay: Long = 0, period: Long, task: (ctrl: TaskControl) -> Unit): TaskControl
    fun onStop(task: () -> Unit): TaskControl
    fun onImmediate(task: () -> Unit): TaskControl
    fun onDestroy(task: () -> Unit): TaskControl
    fun taskGroup(): TaskManagerControl
}