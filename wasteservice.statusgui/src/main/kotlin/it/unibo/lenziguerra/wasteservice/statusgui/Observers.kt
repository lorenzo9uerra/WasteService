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
import unibo.actor22comm.utils.ColorsOut
import java.util.*

class TrolleyObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private var lastState = TrolleyStatus.State.STOPPED
    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val tState = TrolleyStatus.fromProlog(content)
        ColorsOut.outappl("Obs Trolley | state: ${tState.status}", ColorsOut.GREEN)
        if (lastState != tState.status) {
            lastState = tState.status
            for (ws in wsList) {
                ws.sendMessage(TextMessage("trolleyState: ${tState.status}"))
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING TROLLEY FAILED")
    }
}

class StorageObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private var lastGlass: Float = -1.0f
    private var lastPlastic = -1.0f

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val sStatus = StorageStatus.fromProlog(content)
        if(sStatus.amounts.isNotEmpty()) {
            if (sStatus.amounts[WasteType.GLASS] != lastGlass) {
                lastGlass = sStatus.amounts[WasteType.GLASS]!!
                for (ws in wsList) {
                    ws.sendMessage(TextMessage("depositedGlass: ${sStatus.amounts[WasteType.GLASS]}"))
                }
            }
            if (sStatus.amounts[WasteType.PLASTIC] != lastPlastic) {
                lastPlastic = sStatus.amounts[WasteType.PLASTIC]!!
                for (ws in wsList) {
                    ws.sendMessage(TextMessage("depositedPlastic: ${sStatus.amounts[WasteType.PLASTIC]}"))
                }
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING STORAGE FAILED")
    }
}

class WasteServiceObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private var lastPos = SystemLocation.UNKNOWN

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val wStatus = WasteServiceStatus.fromProlog(content)
        ColorsOut.outappl("Obs WasteService | tpos: ${wStatus.trolleyPos}", ColorsOut.GREEN)
        if (lastPos != wStatus.trolleyPos) {
            lastPos = wStatus.trolleyPos
            for (ws in wsList) {
                ws.sendMessage(TextMessage("trolleyPosition: ${wStatus.trolleyPos}"))
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING WASTESERVICE FAILED")
    }
}

class LedObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private var lastState = BlinkLedState.OFF

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val lState = LedStatus.fromProlog(content)
        ColorsOut.outappl("Obs Led | state: ${lState.state}", ColorsOut.GREEN)
        if (lastState != lState.state) {
            lastState = lState.state
            for (ws in wsList) {
                ws.sendMessage(TextMessage("ledState: ${lState.state}"))
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING LED FAILED")
    }
}