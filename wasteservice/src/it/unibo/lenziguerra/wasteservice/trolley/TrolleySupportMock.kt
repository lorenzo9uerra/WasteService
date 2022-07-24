package it.unibo.lenziguerra.wasteservice.trolley

import unibo.actor22comm.utils.ColorsOut

class TrolleySupportMock : AbstractTrolleyVirtual(getPositions()) {
    override fun init() {
        ColorsOut.outappl("Initialized StorageManager Mock!", ColorsOut.CYAN)
    }

    companion object {
        fun getPositions(): Map<String, Array<Int>> {
            val map = mutableMapOf("home" to arrayOf(0, 0))
            map["indoor"] = arrayOf(0, 1)
            map["glass"] = arrayOf(4, 0)
            map["papera"] = arrayOf(6, 2)
            return map
        }
    }
}