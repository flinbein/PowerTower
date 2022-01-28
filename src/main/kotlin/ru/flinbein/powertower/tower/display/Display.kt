package ru.flinbein.powertower.tower.display

import ru.flinbein.powertower.annotations.TowerControl

@TowerControl(Display.Controller::class)
interface Display {
    var displayText: String
}