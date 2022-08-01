package it.unibo.lenziguerra.wasteservice.data

import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils

data class TrolleyStatus (val status: String, val pos: Array<Int>, val contentType: WasteType?, val contentAmount: Float) {
    companion object {
        fun fromProlog(prolStr: String): TrolleyStatus {
            val status = PrologUtils.extractPayload(PrologUtils.getFuncLine(prolStr, "status")!!)[0]
            val pos = PrologUtils.extractPayload(PrologUtils.getFuncLine(prolStr, "status")!!)
            val contentLine = PrologUtils.getFuncLine(prolStr, "content")

            if (contentLine == null) {
                return TrolleyStatus(status, pos.map{it.toInt()}.toTypedArray(), null, 0f)
            } else {
                val contentArgs = PrologUtils.extractPayload(contentLine)
                val wasteType = WasteType.values().find { it.id == contentArgs[0] }
                return TrolleyStatus(status, pos.map{it.toInt()}.toTypedArray(), wasteType, contentArgs[1].toFloat())
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrolleyStatus

        if (status != other.status) return false
        if (!pos.contentEquals(other.pos)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + pos.contentHashCode()
        return result
    }
}