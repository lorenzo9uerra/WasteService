package it.unibo.lenziguerra.wasteservice.led

import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.radarSystem22.domain.DeviceFactory
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig
import unibo.comm22.utils.CommSystemConfig

fun main() {
    // Config
    CommSystemConfig.tracing = true;

    SystemConfig.setConfiguration()

    DomainSystemConfig.setTheConfiguration("LedConfiguration.json")
    DomainSystemConfig.sonarAvailable = false;
    DomainSystemConfig.radarAvailable = false;

    // Integration
    val path = "${SystemConfig.ctxNames["trolley"]!!}/${SystemConfig.actorNames["trolley"]!!}"

    val led = BlinkLed(DeviceFactory.createLed())
    val ledController = LedController(led)
    val ledServer = BlinkLedCoapServer(SystemConfig.ports["led"]!!, led)

    led.updateHandler = { ledServer.sendUpdates() }

    ledController.connect(SystemConfig.hosts["trolley"]!!, SystemConfig.ports["trolley"]!!, path)
    ledServer.start()
}