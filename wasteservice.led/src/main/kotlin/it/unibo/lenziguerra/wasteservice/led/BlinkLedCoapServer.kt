package it.unibo.lenziguerra.wasteservice.led

import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.server.resources.CoapExchange
import unibo.actor22comm.utils.ColorsOut

class BlinkLedCoapServer(val port: Int, val led: BlinkLed) {
    val server = CoapServer(port)
    private val resource = CoapResourceBlinkLed("led", led)

    init {
        server.add(resource)
    }

    fun start() {
        server.start()
        ColorsOut.out("Started BlinkLedCoapServer, port $port", ColorsOut.ANSI_PURPLE)
    }

    fun stop() {
        server.stop()
        ColorsOut.out("Stopped BlinkLedCoapServer, port $port", ColorsOut.ANSI_PURPLE)
    }

    fun sendUpdates() {
        resource.changed()
    }
}

class CoapResourceBlinkLed(name: String, val led: BlinkLed) : CoapResource(name) {
    init {
        isObservable = true
    }

    override fun handleGET(exchange: CoapExchange) {
        // ledStatus(on|off|blinking)
        exchange.respond( "ledStatus(${led.status.name.lowercase()})")
    }

    override fun handlePOST(exchange: CoapExchange) {
        ColorsOut.outappl("CoapResourceBlinkLed | warn: received POST, not implemented", ColorsOut.YELLOW)
        exchange.respond( "POST not implemented")
    }

    override fun handlePUT(exchange: CoapExchange) {
        ColorsOut.outappl("CoapResourceBlinkLed | warn: received PUT, not implemented", ColorsOut.YELLOW)
        exchange.respond( "PUT not implemented")
    }

    override fun handleDELETE(exchange: CoapExchange) {
        ColorsOut.outerr("CoapResourceBlinkLed | warn: received DELETE, not allowed")
        exchange.respond( "DELETE not allowed")
    }
}