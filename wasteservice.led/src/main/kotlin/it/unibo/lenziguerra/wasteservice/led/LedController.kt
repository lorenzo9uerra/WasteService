package it.unibo.lenziguerra.wasteservice.led

import it.unibo.lenziguerra.wasteservice.CoapConnectionEndpoint
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.lenziguerra.wasteservice.SystemLocation
import it.unibo.lenziguerra.wasteservice.data.TrolleyStatus
import it.unibo.lenziguerra.wasteservice.data.WasteServiceStatus
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import unibo.comm22.coap.CoapConnection
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommUtils
import java.io.IOException
import kotlin.concurrent.thread


class LedController(val led: IBlinkLed) {
    private val trolleyCoapHandler = TrolleyCoapHandler()
    private val wsCoapHandler = WsCoapHandler()
    private lateinit var coapConnection: CoapConnection

    // Assume initial state: home true, stopped false
    private var atHome = true
    private var stopped = false

    private var doErrorBlink = false

    fun connect(trolleyCoapEndpoint: CoapConnectionEndpoint, wasteserviceCoapEndpoint: CoapConnectionEndpoint) {
        trolleyCoapEndpoint.let {
            try {
                coapConnection = CoapConnection("${it.host}:${it.port}", it.uri)
                coapConnection.observeResource(trolleyCoapHandler)
                ColorsOut.outappl("connected trolley via Coap conn: $${it.host}:$${it.port}/${it.uri}", ColorsOut.CYAN)
            } catch (e: Exception) {
                e.printStackTrace()
                ColorsOut.outerr("COaP connection error:" + e.message)
            }
        }
        wasteserviceCoapEndpoint.let {
            try {
                coapConnection = CoapConnection("${it.host}:${it.port}", it.uri)
                coapConnection.observeResource(wsCoapHandler)
                ColorsOut.outappl("connected wasteservice via Coap conn: $${it.host}:$${it.port}/${it.uri}", ColorsOut.CYAN)
            } catch (e: Exception) {
                e.printStackTrace()
                ColorsOut.outerr("COaP connection error:" + e.message)
            }
        }
    }

    inner class TrolleyCoapHandler : CoapHandler {
        override fun onLoad(response: CoapResponse?) {
            val respText = response?.responseText ?: throw IOException("LedController.TrolleyCoapHandler | Empty response!")
            val respStatus = TrolleyStatus.fromProlog(respText)

            ColorsOut.out("LedController.TrolleyCoapHandler | received $respText\nData is $respStatus", ColorsOut.BLACK)

            stopped = respStatus.status == TrolleyStatus.State.STOPPED

            if (stopped) {
                led.turnOff()
            } else if (respStatus.status == TrolleyStatus.State.WORK) {
                if (atHome) {
                    led.turnOn()
                } else {
                    led.blink()
                }
            } else if (respStatus.status == TrolleyStatus.State.ERROR) {
                if (!doErrorBlink) {
                    doErrorBlink = true
                    thread {
                        while(doErrorBlink) {
                            repeat (2) {
                                if (doErrorBlink)
                                    led.turnOn()
                                CommUtils.delay(100)
                                if (doErrorBlink)
                                    led.turnOff()
                                CommUtils.delay(100)
                            }
                            CommUtils.delay(800)
                        }
                    }
                }
            } else {
                ColorsOut.outerr("LedController.TrolleyCoapHandler | Unknown status ${respStatus.status}")
            }

            doErrorBlink = respStatus.status == TrolleyStatus.State.ERROR
        }

        override fun onError() {
            ColorsOut.outerr("COaP error!")
        }
    }

    inner class WsCoapHandler : CoapHandler {
        override fun onLoad(response: CoapResponse?) {
            val respText = response?.responseText ?: throw IOException("LedController.WsCoapHandler | Empty response!")
            val respStatus = WasteServiceStatus.fromProlog(respText)

            ColorsOut.out("LedController.WsCoapHandler | received $respText\nData is $respStatus", ColorsOut.BLACK)

            atHome = respStatus.trolleyPos == SystemLocation.HOME

            if (!stopped) {
                if (atHome) {
                    led.turnOn()
                } else {
                    led.blink()
                }
            }
        }

        override fun onError() {
            ColorsOut.outerr("COaP error!")
        }
    }
}