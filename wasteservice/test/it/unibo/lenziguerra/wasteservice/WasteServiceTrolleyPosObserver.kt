package it.unibo.lenziguerra.wasteservice

import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import unibo.actor22comm.utils.ColorsOut

class WasteServiceTrolleyPosObserver : CoapHandler {
    private val history: MutableList<String> = ArrayList()

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val payload: List<String> = PrologUtils.extractPayload(PrologUtils.getFuncLine(content, "tpos")!!)
        ColorsOut.outappl("Obs WSTrolley | tpos: " + payload[0], ColorsOut.GREEN)
        val newPos = payload[0]
        var add = history.size == 0
        if (!add) {
            val last = history[history.size - 1]
            add = last != newPos
        }
        if (add) history.add(newPos)
        ColorsOut.outappl("Obs WSTrolley | tpos history: $history", ColorsOut.GREEN)
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)")
    }

    fun getHistory(): List<String>? {
        return history
    }

}