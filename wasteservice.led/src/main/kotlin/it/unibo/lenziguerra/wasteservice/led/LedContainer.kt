package it.unibo.lenziguerra.wasteservice.led

import it.unibo.lenziguerra.wasteservice.CoapConnectionEndpoint
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.radarSystem22.domain.DeviceFactory
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import kotlin.concurrent.thread

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

class LedContainer(
    serverPort: Int = SystemConfig.ports["led"]!!,
    val led: IBlinkLed = BlinkLed(DeviceFactory.createLed()),
    val ledController: LedController = LedController(led),
    val ledServer: BlinkLedCoapServer = BlinkLedCoapServer(serverPort, led),
) {

    init {
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

        Runtime.getRuntime().addShutdownHook(thread(start=false, block={
            led.turnOff()
            ColorsOut.outappl("Goodbye! Turning off led", ColorsOut.CYAN)
            Thread.sleep(100)
        }))

        ledController.connect(trolleyCoapEndpoint, wsCoapEndpoint)
        ledServer.start()
    }
}