package it.unibo.lenziguerra.wasteservice.led

import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.lenziguerra.wasteservice.data.TrolleyStatus
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import unibo.actor22comm.coap.CoapConnection
import unibo.actor22comm.utils.ColorsOut
import java.io.IOException
import kotlin.concurrent.thread


class LedController(val led: BlinkLed) {
    private val coapHandler = LedCoapHandler()
    private lateinit var coapConnection: CoapConnection

    fun connect(host: String, port: Int, uri: String) {
        try {
            coapConnection = CoapConnection("$host:$port", uri)
            coapConnection.observeResource(coapHandler)
            ColorsOut.outappl("connected via Coap conn: $coapConnection", ColorsOut.CYAN)
        } catch (e: Exception) {
            e.printStackTrace()
            ColorsOut.outerr("COaP connection error:" + e.message)
        }
    }

    inner class LedCoapHandler : CoapHandler {
        override fun onLoad(response: CoapResponse?) {
            val respText = response?.responseText ?: throw IOException("LedCoapHandler | Empty response!")
            val respStatus = TrolleyStatus.fromProlog(respText)

            ColorsOut.out("LedCoapHandler | received $respText", ColorsOut.BLACK)

            if (respStatus.status == "stopped") {
                led.turnOff()
            } else if (respStatus.status == "work") {
                if (isPosInBounds(respStatus.pos, SystemConfig.positions["home"]!!)) {
                    led.turnOn()
                } else {
                    led.blink()
                }
            } else {
                ColorsOut.outerr("LedCoapHandler | Unknown status ${respStatus.status}")
            }
        }

        override fun onError() {
            ColorsOut.outerr("COaP error!")
        }
    }

    fun isPosInBounds(pos: Array<Int>, bounds: List<List<Int>>): Boolean {
        return pos[0] >= bounds[0][0] && pos[0] <= bounds[1][0] &&
                pos[1] >= bounds[0][1] && pos[1] <= bounds[1][1]
    }
}