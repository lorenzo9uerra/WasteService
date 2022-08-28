package it.unibo.lenziguerra.wasteservice

import it.unibo.lenziguerra.wasteservice.data.WasteServiceStatus
import it.unibo.lenziguerra.wasteservice.utils.LogUtils
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import unibo.comm22.utils.ColorsOut
import java.util.concurrent.Semaphore

class WasteServiceTrolleyPosObserver : CoapHandler {
    val history: MutableList<String> = ArrayList()
    val semaphore: Semaphore = Semaphore(0)
    var testend = false // for cleanup

    override fun onLoad(response: CoapResponse) {
        if (testend) return

        val content = response.responseText
        val newPos = WasteServiceStatus.fromProlog(content).trolleyPos.name.lowercase()
        LogUtils.threadOut("Obs WSTrolley ${hashCode()} | tpos: " + newPos, ColorsOut.GREEN)

        var isRelevantUpdate = history.size == 0
        if (!isRelevantUpdate) {
            val last = history[history.size - 1]
            isRelevantUpdate = last != newPos
        }
        isRelevantUpdate = isRelevantUpdate && !canSkip(newPos)
        if (isRelevantUpdate) {
            history.add(newPos)
            LogUtils.threadOut("Obs WSTrolley | tpos history: $history", ColorsOut.GREEN)

            if (semaphore.availablePermits() == 0) {
                semaphore.release()
            }
        }
    }

    override fun onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)")
    }

    private fun canSkip(pos: String): Boolean {
        return pos in arrayOf( "travel" )
    }
}