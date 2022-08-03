package it.unibo.lenziguerra.wasteservice.trolley

import unibo.actor22comm.utils.ColorsOut

class TrolleySupportMock : AbstractTrolleyVirtual() {
    override fun init() {
        ColorsOut.outappl("Initialized Trolley Mock!", ColorsOut.CYAN)
    }
}