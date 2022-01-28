package ru.flinbein.powertower.tower

import de.Ste3et_C0st.FurnitureLib.Crafting.Project
import de.Ste3et_C0st.FurnitureLib.main.ObjectID
import ru.flinbein.powertower.service.TowerHandler


abstract class FurnitureProjectTower(hd: TowerHandler<out FurnitureProjectTower>): FurnitureTower(hd) {

    abstract fun getFurnitureProject(): Project

    override fun buildFurniture(): ObjectID {
        return furnitureLib.spawn(getFurnitureProject(), location)
    }
}
