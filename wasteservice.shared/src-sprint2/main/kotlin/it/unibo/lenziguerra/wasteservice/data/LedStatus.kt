package it.unibo.lenziguerra.wasteservice.data

import it.unibo.lenziguerra.wasteservice.BlinkLedState
import it.unibo.lenziguerra.wasteservice.SystemLocation
import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils

data class LedStatus (val state: BlinkLedState) {
    companion object {
        fun fromProlog(prolStr: String): LedStatus {
            val stateStr = PrologUtils.getFuncLine(prolStr, "ledState")?.let {
                PrologUtils.extractPayload(it)[0]
            } ?: throw IllegalArgumentException("Wrong string for LedStatus: $prolStr")

            return LedStatus(BlinkLedState.valueOf(stateStr.uppercase()))
        }
    }

    override fun toString(): String {
        return "ledState(${state.name.lowercase()})"
    }
}