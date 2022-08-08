package it.unibo.lenziguerra.wasteservice.storage

import it.unibo.lenziguerra.wasteservice.WasteType
import unibo.comm22.utils.ColorsOut

class StorageManagerSupportMock : AbstractStorageManagerVirtual(getMaxAmounts()) {
    override fun init() {
        ColorsOut.outappl("Initialized StorageManager Mock!", ColorsOut.CYAN)
    }

    companion object {
        fun getMaxAmounts(): Map<WasteType, Float> {
            return WasteType.values().associateWith { 50f }
        }
    }
}