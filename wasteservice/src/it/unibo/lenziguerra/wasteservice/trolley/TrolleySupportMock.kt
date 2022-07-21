package it.unibo.lenziguerra.wasteservice.trolley

import unibo.actor22comm.utils.ColorsOut

class TrolleySupportMock : AbstractTrolleyVirtual(getPositions()) {
    override fun init() {
        ColorsOut.outappl("Initialized StorageManager Mock!", ColorsOut.CYAN)
    }

    companion object {
        fun getPositions(): Map<String, Array<Int>> {
            val map = mutableMapOf<String, Array<Int>>("HOME" to arrayOf<Int>(0, 0))
            map["INDOOR"] = arrayOf<Int>(1, 15)
            map["GLASS"] = arrayOf<Int>(13, 0)
            map["PAPER"] = arrayOf<Int>(16, 4)
            return map
        }
    }
}