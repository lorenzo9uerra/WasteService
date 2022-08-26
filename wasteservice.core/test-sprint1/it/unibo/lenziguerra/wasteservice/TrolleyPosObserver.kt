package it.unibo.lenziguerra.wasteservice

import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils.getFuncLine
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import unibo.comm22.utils.ColorsOut
import java.util.*
import java.util.stream.Collectors

class TrolleyPosObserver : CoapHandler {
    val history: MutableList<IntArray> = ArrayList()

    override fun onLoad(response: CoapResponse) {
        val content = response.responseText
        val payload = PrologUtils.extractPayload(getFuncLine(content, "pos")!!)
        ColorsOut.outappl("Obs Trolley | pos: " + payload[0] + "," + payload[1], ColorsOut.GREEN)
        val newPos = intArrayOf(payload[0].toInt(), payload[1].toInt())
        var add = history.size == 0
        if (!add) {
            val last = history[history.size - 1]
            add = last[0] != newPos[0] || last[1] != newPos[1]
        }
        if (add) history.add(newPos)
        ColorsOut.outappl("Obs Trolley | pos history: " + history.stream().map { a: IntArray? -> Arrays.toString(a) }
            .collect(Collectors.joining(",")), ColorsOut.GREEN)
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)")
    }
}