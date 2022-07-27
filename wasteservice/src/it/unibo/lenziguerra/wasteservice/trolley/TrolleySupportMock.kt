package it.unibo.lenziguerra.wasteservice.trolley

import unibo.actor22comm.utils.ColorsOut

class TrolleySupportMock : AbstractTrolleyVirtual(getPositions()) {
    override fun init() {
        ColorsOut.outappl("Initialized StorageManager Mock!", ColorsOut.CYAN)
    }

    companion object {
        fun getPositions(): Map<String, Array<Int>> {
            val map = mutableMapOf("home" to arrayOf(0, 0))
            map["indoor"] = arrayOf(0, 2)
            map["glass_box"] = arrayOf(2, 2)
            map["paper_box"] = arrayOf(2, 0)
            return map
        }
    }
}