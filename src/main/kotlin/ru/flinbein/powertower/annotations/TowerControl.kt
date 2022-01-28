package ru.flinbein.powertower.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TowerControl(val controllerClass: KClass<*>)
