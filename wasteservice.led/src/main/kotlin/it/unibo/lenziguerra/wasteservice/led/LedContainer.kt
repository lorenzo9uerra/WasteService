package it.unibo.lenziguerra.wasteservice.led

import it.unibo.lenziguerra.wasteservice.CoapConnectionEndpoint
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
    LedContainer().start()
}

class LedContainer(serverPort: Int = SystemConfig.ports["led"]!!) {
    val led: IBlinkLed
    val ledController: LedController
    val ledServer: BlinkLedCoapServer

    init {
        led = BlinkLed(DeviceFactory.createLed())
        ledController = LedController(led)
        ledServer = BlinkLedCoapServer(serverPort, led)

        led.updateHandler = { ledServer.sendUpdates() }
    }

    fun start() {
        val trolleyCoapEndpoint = CoapConnectionEndpoint(
            SystemConfig.hosts["trolley"]!!, SystemConfig.ports["trolley"]!!,
            "${SystemConfig.contexts["trolley"]!!}/${SystemConfig.actors["trolley"]!!}"
        )
        val wsCoapEndpoint = CoapConnectionEndpoint(
            SystemConfig.hosts["wasteServiceContext"]!!, SystemConfig.ports["wasteServiceContext"]!!,
            "${SystemConfig.contexts["wasteServiceContext"]!!}/${SystemConfig.actors["wasteServiceContext"]!!}"
        )

        ledController.connect(trolleyCoapEndpoint, wsCoapEndpoint)
        ledServer.start()
    }
}