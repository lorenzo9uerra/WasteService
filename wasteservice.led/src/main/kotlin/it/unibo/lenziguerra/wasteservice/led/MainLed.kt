package it.unibo.lenziguerra.wasteservice.led

import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.radarSystem22.domain.DeviceFactory
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig

fun main() {
    SystemConfig.setConfiguration()
    DomainSystemConfig.setTheConfiguration("LedConfiguration.json")
    DomainSystemConfig.sonarAvailable = false;
    DomainSystemConfig.radarAvailable = false;

    val path = "${SystemConfig.ctxNames["trolley"]!!}/${SystemConfig.actorNames["trolley"]!!}"

    val ledController = LedController(BlinkLed(DeviceFactory.createLed()))
    ledController.connect(SystemConfig.hosts["trolley"]!!, SystemConfig.ports["trolley"]!!, path)
}