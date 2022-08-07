package it.unibo.lenziguerra.wasteservice.trolley

import it.unibo.lenziguerra.wasteservice.WasteType
import unibo.actor22comm.utils.ColorsOut
import unibo.actor22comm.utils.CommUtils

class TrolleySupportMock : AbstractTrolleyVirtual() {
    var collectDepositDelay = 1000

    override fun init() {
        ColorsOut.outappl("Initialized Trolley Mock!", ColorsOut.CYAN)

        // Togli attesa durante i test; è in ogni caso fittizia, utile in esecuzione
        // reale per capire meglio l'attività attuale, ma non ha altri scopi.
        // NOTA: Nella maggior parte dei casi modificare il comportamento in caso di
        // test è una pessima idea per molte ragioni; qua viene fatto solo essendo una cosa
        // "decorativa" per una classe di mock.
        var isJunit = false
        for (element in Thread.currentThread().stackTrace) {
            if (element.className.startsWith("org.junit.")) {
                isJunit = true
                break
            }
        }
        if (isJunit) {
            collectDepositDelay = 0
        }
    }

    override fun doCollect(material: WasteType, quantity: Float): Boolean {
        CommUtils.delay(collectDepositDelay)
        return true;
    }

    override fun doDeposit(material: WasteType, quantity: Float): Boolean {
        CommUtils.delay(collectDepositDelay)
        return true;
    }
}