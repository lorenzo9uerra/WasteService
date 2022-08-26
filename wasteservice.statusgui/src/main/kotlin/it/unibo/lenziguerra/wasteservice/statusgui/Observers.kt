package it.unibo.lenziguerra.wasteservice.statusgui

import it.unibo.lenziguerra.wasteservice.BlinkLedState
import it.unibo.lenziguerra.wasteservice.SystemLocation
import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.data.LedStatus
import it.unibo.lenziguerra.wasteservice.data.StorageStatus
import it.unibo.lenziguerra.wasteservice.data.TrolleyStatus
import it.unibo.lenziguerra.wasteservice.data.WasteServiceStatus
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import unibo.comm22.utils.ColorsOut
import java.util.*

class TrolleyObserver(private val wsList: ArrayList<WebSocketSession>) : CoapHandler {
    var lastStatus = TrolleyStatus(TrolleyStatus.State.STOPPED, arrayOf(-1,-1),
        null, 0f, TrolleyStatus.Activity.IDLE
    )
        private set

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        if (isSkippableResponse(content)) return

        val tStatus = TrolleyStatus.fromProlog(content)
        ColorsOut.outappl("Obs Trolley | state: ${tStatus.status} activity: ${tStatus.activity}", ColorsOut.GREEN)
        if (lastStatus.status != tStatus.status) {
            for (ws in wsList) {
                synchronized(ws) {
                    ws.sendMessage(TextMessage("trolleyState: ${tStatus.status}"))
                }
            }
        }
        if (lastStatus.activity != tStatus.activity) {
            for (ws in wsList) {
                synchronized(ws) {
                    ws.sendMessage(TextMessage("trolleyActivity: ${tStatus.activity}"))
                }
            }
        }

        lastStatus = tStatus
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING TROLLEY FAILED")
    }
}

class StorageObserver(private val wsList: ArrayList<WebSocketSession>) : CoapHandler {
    var lastGlass: Float = -1.0f
        private set
    var lastPlastic = -1.0f
        private set

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        if (isSkippableResponse(content)) return

        val sStatus = StorageStatus.fromProlog(content)
        ColorsOut.outappl("Obs Storage | contents: ${sStatus.amounts}", ColorsOut.GREEN)

        if(sStatus.amounts.isNotEmpty()) {
            if (sStatus.amounts[WasteType.GLASS] != lastGlass) {
                lastGlass = sStatus.amounts[WasteType.GLASS]!!
                for (ws in wsList) {
                    synchronized(ws) {
                        ws.sendMessage(TextMessage("depositedGlass: ${sStatus.amounts[WasteType.GLASS]}"))
                    }
                }
            }
            if (sStatus.amounts[WasteType.PLASTIC] != lastPlastic) {
                lastPlastic = sStatus.amounts[WasteType.PLASTIC]!!
                for (ws in wsList) {
                    synchronized(ws) {
                        ws.sendMessage(TextMessage("depositedPlastic: ${sStatus.amounts[WasteType.PLASTIC]}"))
                    }
                }
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING STORAGE FAILED")
    }
}

class WasteServiceObserver(private val wsList: ArrayList<WebSocketSession>) : CoapHandler {
    var lastPos = SystemLocation.UNKNOWN
        private set

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        if (isSkippableResponse(content)) return

        val wStatus = WasteServiceStatus.fromProlog(content)
        ColorsOut.outappl("Obs WasteService | tpos: ${wStatus.trolleyPos}", ColorsOut.GREEN)
        if (lastPos != wStatus.trolleyPos) {
            lastPos = wStatus.trolleyPos
            for (ws in wsList) {
                synchronized(ws) {
                    ws.sendMessage(TextMessage("trolleyPosition: ${wStatus.trolleyPos}"))
                }
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING WASTESERVICE FAILED")
    }
}

class LedObserver(private val wsList: ArrayList<WebSocketSession>) : CoapHandler {
    var lastState = BlinkLedState.OFF
        private set

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        if (isSkippableResponse(content)) return

        val lState = LedStatus.fromProlog(content)
        ColorsOut.outappl("Obs Led | state: ${lState.state}", ColorsOut.GREEN)
        if (lastState != lState.state) {
            lastState = lState.state
            for (ws in wsList) {
                synchronized(ws) {
                    ws.sendMessage(TextMessage("ledState: ${lState.state}"))
                }
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING LED FAILED")
    }
}

// Initial or blank response
fun isSkippableResponse(content: String): Boolean {
    return content.isBlank() ||
        (
            content.contains("ActorBasic(Resource)")
            && content.contains("created")
        )
}