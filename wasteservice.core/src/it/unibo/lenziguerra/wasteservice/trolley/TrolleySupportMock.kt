package it.unibo.lenziguerra.wasteservice.trolley

import it.unibo.lenziguerra.wasteservice.WasteType
import kotlinx.coroutines.delay
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommUtils

class TrolleySupportMock : AbstractTrolleyVirtual() {
    var collectDepositDelay = 1000L

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

    override suspend fun doCollect(material: WasteType, quantity: Float): Boolean {
        delay(collectDepositDelay)
        return true;
    }

    override suspend fun doDeposit(material: WasteType, quantity: Float): Boolean {
        delay(collectDepositDelay)
        return true;
    }
}