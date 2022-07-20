package it.unibo.storagemanager

import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.storage.AbstractStorageManagerVirtual
import unibo.actor22comm.utils.ColorsOut

class StorageManagerMock : AbstractStorageManagerVirtual(getMaxAmounts()) {
    override fun init() {
        ColorsOut.outappl("Initialized StorageManager Mock!", ColorsOut.CYAN)
    }

    companion object {
        fun getMaxAmounts(): Map<String, Float> {
            return WasteType.values().associate { it.id to 50f }
        }
    }
}