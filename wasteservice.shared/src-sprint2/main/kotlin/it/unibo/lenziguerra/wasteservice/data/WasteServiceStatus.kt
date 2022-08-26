package it.unibo.lenziguerra.wasteservice.data

import it.unibo.lenziguerra.wasteservice.SystemLocation
import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils

data class WasteServiceStatus (val trolleyPos: SystemLocation, val error: String?) {
    companion object {
        fun fromProlog(prolStr: String): WasteServiceStatus {
            val trolleyPosStr = PrologUtils.getFuncLine(prolStr, "tpos")?.let {
                PrologUtils.extractPayload(it)[0]
            } ?: throw IllegalArgumentException("Wrong string for WasteServiceStatus: $prolStr")

            var trolleyPos = SystemLocation.UNKNOWN
            trolleyPos = try{
                SystemLocation.valueOf(trolleyPosStr.uppercase())
            }catch (_: Exception){
                trolleyPos
            }
            val errorLine = PrologUtils.getFuncLine(prolStr, "error")
            val error = errorLine?.let { PrologUtils.extractPayload(it)[0] }

            return WasteServiceStatus(trolleyPos, error)
        }
    }

    override fun toString(): String {
        return "tpos(${trolleyPos.name.lowercase()})" + (error?.let { "\nerror($error)" } ?: "")
    }
}