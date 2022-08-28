package it.unibo.lenziguerra.wasteservice

import it.unibo.lenziguerra.wasteservice.data.TrolleyStatus
import it.unibo.lenziguerra.wasteservice.utils.LogUtils
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import unibo.comm22.utils.ColorsOut
import java.util.*
import java.util.concurrent.Semaphore
import java.util.stream.Collectors

class TrolleyPosObserver() : CoapHandler {
    val history: MutableList<IntArray> = ArrayList()
    val semaphore: Semaphore = Semaphore(0)
    var testend = false // for cleanup

    override fun onLoad(response: CoapResponse) {
        if (testend) return

        val content = response.responseText
        val pos = TrolleyStatus.fromProlog(content).pos
        LogUtils.threadOut("Obs Trolley ${hashCode()} | pos: ${pos.contentToString()}", ColorsOut.GREEN)
        val newPos = intArrayOf(pos[0], pos[1])
        var isRelevantUpdate = history.size == 0
        if (!isRelevantUpdate) {
            val last = history[history.size - 1]
            isRelevantUpdate = last[0] != newPos[0] || last[1] != newPos[1]
        }
        if (isRelevantUpdate) {
            history.add(newPos)
            LogUtils.threadOut("Obs Trolley | pos history: ${history.map { it.contentToString() }}", ColorsOut.GREEN)

            if (semaphore.availablePermits() == 0) {
                semaphore.release()
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)")
    }
}