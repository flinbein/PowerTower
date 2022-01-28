package ru.flinbein.powertower.tower

import ru.flinbein.powertower.service.TowerHandler
import ru.flinbein.powertower.tower.aimable.Aimable

class TestTower(init: TowerHandler<TestTower>): Tower(init), Aimable by init() {


}
