package it.unibo.lenziguerra.wasteservice.storage

import it.unibo.lenziguerra.wasteservice.WasteType
import unibo.actor22comm.utils.ColorsOut

class StorageManagerSupportMock : AbstractStorageManagerVirtual(getMaxAmounts()) {
    override fun init() {
        ColorsOut.outappl("Initialized StorageManager Mock!", ColorsOut.CYAN)
    }

    companion object {
        fun getMaxAmounts(): Map<String, Float> {
            return WasteType.values().associate { it.id to 50f }
        }
    }
}