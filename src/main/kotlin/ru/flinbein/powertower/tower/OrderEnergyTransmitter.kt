package ru.flinbein.powertower.tower

import me.dpohvar.powernbt.api.NBTCompound
import org.bukkit.World
import ru.flinbein.powertower.annotations.TowerControl
import ru.flinbein.powertower.service.TaskControl
import ru.flinbein.powertower.service.TaskManager
import ru.flinbein.powertower.service.TowerHandler
import java.util.*

@TowerControl(OrderEnergyTransmitter.Controller::class)
interface OrderEnergyTransmitter: EnergyTransmitter {

    var energyTransmitPeriod: Long
    var energyTransmitAmount: Double

    fun addReceiver(tower: Tower)
    fun removeReceiver(index: Int)

    fun beforeTransmitAnimation(task: TaskManager): Long = 0
    fun transmitAnimation(transmitted: Double, receiver: EnergyReceiver, task: TaskManager): Long = 0
    fun afterTransmitAnimation(transmitted: Double, receiver: EnergyReceiver?, task: TaskManager): Long = 0

    class Controller(private val hd: TowerHandler<Tower>): OrderEnergyTransmitter, EnergyHolder by hd(), TowerEvents {

        override var energyTransmitPeriod: Long = 0
            set(value) {
                field = value
                startTransmitEnergy(value)
            }
        override var energyTransmitAmount: Double = .0

        private val towerTransmitter: OrderEnergyTransmitter by lazy { hd.tower as OrderEnergyTransmitter }

        private var transmitTask: TaskControl? = null
        private fun startTransmitEnergy(period: Long) {
            transmitTask?.stop()
            if (period <= 0) return
            val taskGroup = hd.taskGroup()
            transmitTask = taskGroup
            taskGroup.onTimer(period, period) { this.transmitEnergyNext(taskGroup) }
        }

        private fun transmitEnergyNext(task: TaskManager){
            if (energy <= 0) return
            if (energyTransmitAmount <= 0) return
            val size = receivers.size
            if (size <= 0) return

            val animationBeforeDelay = towerTransmitter.beforeTransmitAnimation(task)
            task.onTimeout(animationBeforeDelay) {
                var transmitDelay = 0L
                var transmitted = .0
                var receiver: EnergyReceiver? = null
                for (ignored in 0 until size) {
                    val index = lastEnergyReceiverIndex % size
                    lastEnergyReceiverIndex = (lastEnergyReceiverIndex+1) % size
                    val towerUid = receivers[index]
                    receiver = hd.service[towerUid] ?: continue
                    transmitted = towerTransmitter.transmitEnergy(energyTransmitAmount, receiver)
                    if (transmitted > 0) {
                        transmitDelay = towerTransmitter.transmitAnimation(transmitted, receiver, task)
                        break
                    }
                }
                task.onTimeout(transmitDelay) {
                    towerTransmitter.afterTransmitAnimation(transmitted, receiver, task)
                }
            }

        }

        private lateinit var receivers: ArrayList<UUID>
        private var lastEnergyReceiverIndex: Int = 0

        override fun addReceiver(tower: Tower) {
            receivers.add(tower.uid)
        }

        override fun removeReceiver(index: Int) {
            receivers.removeAt(index)
        }

        override fun load(world: World, data: NBTCompound) {
            super.load(world, data)
            if (data.containsKey("EnergyReceivers")) {
                val receiversId = data.getList("EnergyReceivers").map { it as LongArray }
                receivers = ArrayList( receiversId.map { UUID(it[0], it[1]) } )
            }

            if (data.containsKey("EnergyTransmitPeriod")) {
                energyTransmitPeriod = data.getLong("EnergyTransmitPeriod")
            }
            if (data.containsKey("EnergyTransmitAmount")) {
                energyTransmitAmount = data.getDouble("EnergyTransmitAmount")
            }

            if (data.containsKey("LastEnergyReceiverIndex")) {
                lastEnergyReceiverIndex = data.getInt("LastEnergyReceiverIndex")
            }
        }

        override fun save(data: NBTCompound) {
            super.save(data)
            val receiversId = receivers.map { longArrayOf(it.mostSignificantBits, it.leastSignificantBits) }
            data["EnergyReceivers"] = receiversId
            data["EnergyTransmitPeriod"] = energyTransmitPeriod
            data["LastEnergyReceiverIndex"] = lastEnergyReceiverIndex
            data["EnergyTransmitAmount"] = energyTransmitAmount
        }
    }
}



