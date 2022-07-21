package it.unibo.lenziguerra.wasteservice.trolley

import unibo.actor22comm.utils.ColorsOut

class TrolleySupportMock : AbstractTrolleyVirtual(getPositions()) {
    override fun init() {
        ColorsOut.outappl("Initialized StorageManager Mock!", ColorsOut.CYAN)
    }

    companion object {
        fun getPositions(): Map<String, Array<Int>> {
            val map = mutableMapOf<String, Array<Int>>("HOME" to arrayOf<Int>(0, 0))
            map["INDOOR"] = arrayOf<Int>(0, 1)
            map["GLASS"] = arrayOf<Int>(4, 0)
            map["PAPER"] = arrayOf<Int>(6, 2)
            return map
        }
    }
}