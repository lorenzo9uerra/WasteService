package it.unibo.lenziguerra.wasteservice.data

import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils

data class StorageStatus (val amounts: Map<WasteType, Float>, val maxAmounts: Map<WasteType, Float>) {
    companion object {
        fun fromProlog(prolStr: String): StorageStatus {
            val amounts = mutableMapOf<WasteType, Float>()
            val maxAmounts = mutableMapOf<WasteType, Float>()
            val contentLines = PrologUtils.getFuncLines(prolStr, "content")

            for (line in contentLines) {
                val payloadArgs = PrologUtils.extractPayload(line)
                val typeStr = payloadArgs[0]
                val amount = payloadArgs[1].toFloat()
                val maxAmount = payloadArgs[2].toFloat()
                val type = WasteType.values().find { it.id == typeStr } ?: throw IllegalArgumentException("No such waste type $typeStr")
                amounts[type] = amount
                maxAmounts[type] = maxAmount
            }

            return StorageStatus(amounts, maxAmounts)
        }
    }

    override fun toString(): String {
        return amounts.entries.joinToString("\n") { "content(${it.key},${it.value},${maxAmounts[it.key]})" }
    }
}