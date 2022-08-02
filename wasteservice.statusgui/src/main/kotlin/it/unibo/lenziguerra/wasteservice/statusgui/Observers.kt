package it.unibo.lenziguerra.wasteservice.statusgui

import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.data.StorageStatus
import it.unibo.lenziguerra.wasteservice.data.TrolleyStatus
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import unibo.actor22comm.utils.ColorsOut
import java.util.*

class TrolleyObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private lateinit var lastState : TrolleyStatus.State
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
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)")
    }
}

class StorageObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private var lastGlass : Float = -1.0f
    private var lastPlastic = -1.0f

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val sStatus = StorageStatus.fromProlog(content)
        if(sStatus.amounts[WasteType.GLASS] != lastGlass) {
            lastGlass = sStatus.amounts[WasteType.GLASS]!!
            for (ws in wsList) {
                ws.sendMessage(TextMessage("depositedGlass: ${sStatus.amounts[WasteType.GLASS]}"))
            }
        }
        if(sStatus.amounts[WasteType.PLASTIC] != lastPlastic) {
            lastPlastic = sStatus.amounts[WasteType.PLASTIC]!!
            for (ws in wsList) {
                ws.sendMessage(TextMessage("depositedPlastic: ${sStatus.amounts[WasteType.PLASTIC]}"))
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)")
    }
}

class WasteServiceObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private val history: MutableList<String> = ArrayList()

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val payload: List<String> = PrologUtils.extractPayload(PrologUtils.getFuncLine(content, "tpos")!!)
        ColorsOut.outappl("Obs WasteService | tpos: " + payload[0], ColorsOut.GREEN)
        val newTPos = payload[0]
        var add = history.size == 0
        if (!add) {
            val last = history[history.size - 1]
            add = last != newTPos
        }
        if (add) {
            history.add(newTPos)
            for (ws in wsList) {
                ws.sendMessage(TextMessage("trolleyPosition: $newTPos"))
            }
        }
        ColorsOut.outappl("Obs WasteService | tpos history: $history", ColorsOut.GREEN)
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)")
    }
}

class LedObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private val history: MutableList<String> = ArrayList()

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val payload: List<String> = PrologUtils.extractPayload(PrologUtils.getFuncLine(content, "state")!!)
        ColorsOut.outappl("Obs Led | state: " + payload[0], ColorsOut.GREEN)
        val newState = payload[0]
        var add = history.size == 0
        if (!add) {
            val last = history[history.size - 1]
            add = last != newState
        }
        if (add) {
            history.add(newState)
            for (ws in wsList) {
                ws.sendMessage(TextMessage("ledState: $newState"))
            }
        }
        ColorsOut.outappl("Obs Led | state history: $history", ColorsOut.GREEN)
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)")
    }
}