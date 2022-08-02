package it.unibo.lenziguerra.wasteservice.data

import it.unibo.lenziguerra.wasteservice.BlinkLedState
import it.unibo.lenziguerra.wasteservice.SystemLocation
import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils

data class LedStatus (val state: BlinkLedState) {
    companion object {
        fun fromProlog(prolStr: String): LedStatus {
            val stateStr = PrologUtils.extractPayload(PrologUtils.getFuncLine(prolStr, "ledStatus")!!)[0]

            return LedStatus(BlinkLedState.valueOf(stateStr.uppercase()))
        }
    }

    override fun toString(): String {
        return "ledStatus(${state.name.lowercase()})"
    }
}