package it.unibo.lenziguerra.wasteservice

import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import unibo.actor22comm.utils.ColorsOut
import java.util.*
import java.util.concurrent.Semaphore

class TrolleyObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private val history: MutableList<String> = ArrayList()

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val payload: List<String> = PrologUtils.extractPayload(PrologUtils.getFuncLine(content, "state")!!)
        ColorsOut.outappl("Obs Trolley | state: " + payload[0], ColorsOut.GREEN)
        val newState = payload[0]
        var add = history.size == 0
        if (!add) {
            val last = history[history.size - 1]
            add = last != newState
        }
        if (add) {
            history.add(newState)
            for (ws in wsList) {
                ws.sendMessage(TextMessage("trolleyState: $newState"))
            }
        }
        ColorsOut.outappl("Obs Trolley | state history: $history", ColorsOut.GREEN)
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)")
    }
}

class StorageObserver(private var wsList: ArrayList<WebSocketSession>) : CoapHandler {
    private val history: MutableList<String> = ArrayList()

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val payload: List<String> = PrologUtils.extractPayload(PrologUtils.getFuncLine(content, "content")!!)
        ColorsOut.outappl("Obs Storage | content: " + payload[0], ColorsOut.GREEN)
        val newContent = payload[0]
        var add = history.size == 0
        if (!add) {
            val last = history[history.size - 1]
            add = last != newContent
        }
        if (add) {
            history.add(newContent)
            for (ws in wsList) {
                ws.sendMessage(TextMessage("depositedPlastic: $newContent"))
            }
        }
        ColorsOut.outappl("Obs Storage | content history: $history", ColorsOut.GREEN)
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