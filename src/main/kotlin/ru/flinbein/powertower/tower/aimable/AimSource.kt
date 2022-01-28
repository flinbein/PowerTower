package ru.flinbein.powertower.tower.aimable

import org.bukkit.Location
import ru.flinbein.powertower.annotations.TowerControl
import ru.flinbein.powertower.service.TowerHandler
import ru.flinbein.powertower.tower.Tower

@TowerControl(AimSource.Controller::class)
interface AimSource {

    var aimSource: Location
    val aimSpeed: Double

    fun onChangeAim(loc: Location)

    class Controller<T: Tower>(private val hd: TowerHandler<T>): AimSource {

        private var _aimSource: Location? = null
        override var aimSource: Location
            get(){
                if (_aimSource == null) _aimSource = hd.tower.location
                return _aimSource as Location
            }
            set(value) {
                _aimSource = value
                onChangeAim(value)
            }

        override val aimSpeed = 10.0

        override fun onChangeAim(loc: Location) {}
    }
}

