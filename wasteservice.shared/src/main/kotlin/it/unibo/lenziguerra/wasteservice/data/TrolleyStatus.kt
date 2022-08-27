package it.unibo.lenziguerra.wasteservice.data

import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils

data class TrolleyStatus (val status: State, val pos: Array<Int>, val contentType: WasteType? = null, val contentAmount: Float = 0f,
    val activity: Activity = Activity.IDLE
) {
    constructor(statusStr: String, pos: Array<Int>, contentType: String? = null, contentAmount: Float = 0f, activityStr: String = "idle")
        : this(State.valueOf(statusStr.uppercase()), pos, contentType?.let { WasteType.valueOf(it.uppercase()) }, contentAmount, Activity.valueOf(activityStr.uppercase()))

    enum class State {
        WORK, STOPPED, ERROR
    }
    enum class Activity {
        IDLE, TRAVEL, DEPOSIT, COLLECT
    }

    companion object {
        fun fromProlog(prolStr: String): TrolleyStatus {
            val statusStr = PrologUtils.getFuncLine(prolStr, "state")?.let {
                PrologUtils.extractPayload(it)[0]
            } ?: throw IllegalArgumentException("Wrong string for TrolleyStatus: $prolStr")
            val status = State.valueOf(statusStr.uppercase())

            val activityStr = PrologUtils.getFuncLine(prolStr, "activity")?.let {
                PrologUtils.extractPayload(it)[0]
            } ?: throw IllegalArgumentException("Wrong string for TrolleyStatus: $prolStr")
            val activity = Activity.valueOf(activityStr.uppercase())

            val pos = PrologUtils.getFuncLine(prolStr, "pos")?.let {
                PrologUtils.extractPayload(it)
            } ?: throw IllegalArgumentException("Wrong string for TrolleyStatus: $prolStr")

            val contentLine = PrologUtils.getFuncLine(prolStr, "content")

            if (contentLine == null) {
                return TrolleyStatus(status, pos.map{it.toInt()}.toTypedArray(), null, 0f, activity)
            } else {
                val contentArgs = PrologUtils.extractPayload(contentLine)
                val wasteType = WasteType.values().find { it.id == contentArgs[0] }
                return TrolleyStatus(status, pos.map{it.toInt()}.toTypedArray(), wasteType, contentArgs[1].toFloat(), activity)
            }
        }
    }

    override fun toString(): String {
        return """
            |state(${status.toString().lowercase()})
            |pos(${pos[0]},${pos[1]})
            |activity(${activity.toString().lowercase()})
            |${contentType?.let { "content($contentType,$contentAmount)" } ?: ""}
        """.trimMargin()
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