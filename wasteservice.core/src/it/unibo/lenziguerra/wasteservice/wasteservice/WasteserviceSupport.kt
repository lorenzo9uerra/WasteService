package it.unibo.lenziguerra.wasteservice.wasteservice

import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.lenziguerra.wasteservice.SystemLocation
import it.unibo.lenziguerra.wasteservice.data.WasteServiceStatus
import kotlin.math.abs
import kotlin.math.pow

interface IWasteserviceSupport {
    fun getDestination(location: String, Position: String): String
    fun updateTrolleyPos(location: String)
    fun getPrologContent(): String

    var error: String?
}

open class WasteserviceSupport : IWasteserviceSupport {
    init {
        SystemConfig.setConfiguration()
    }

    var trolleyPos = SystemLocation.HOME
    override var error: String? = null

    override fun updateTrolleyPos(location: String) {
        trolleyPos = SystemLocation.valueOf(location.uppercase())
    }

    override fun getDestination(location: String, Position: String): String {
        val pos = arrayOf(
            Position.split(",")[0].toInt(),
            Position.split(",")[1].toInt(),
        )
        val destarea = SystemConfig.positions[location] ?: throw IllegalArgumentException("Unknown location $location")
        val destCoords = getClosestCoordsOrth(pos, destarea)
        return "${destCoords[0]},${destCoords[1]}"
    }

    protected fun getClosestCoordsOrth(pos: Array<Int>, targetBounds: List<List<Int>>): Array<Int> {
        var x = targetBounds[0][0]
        var y = targetBounds[0][1]
        var minX = 10000
        var minY = 10000
        for (i in targetBounds[0][0]..targetBounds[1][0]) {
            if (abs(pos[0] - i) < minX) {
                x = i
                minX = abs(pos[0] - 1)
            }
        }
        for (i in targetBounds[0][1]..targetBounds[1][1]) {
            if (abs(pos[1] - i) < minY) {
                y = i
                minY = abs(pos[1] - 1)
            }
        }
        return arrayOf(x, y)
    }

    protected fun getClosestCoordsByDistance(pos: Array<Int>, targetBounds: List<List<Int>>): Array<Int> {
        var minDist: Float? = null
        var minX: Int? = null
        var minY: Int? = null

        for (x in targetBounds[0][0]..targetBounds[1][0]) {
            for (y in targetBounds[0][1]..targetBounds[1][1]) {
                val sqDist = (x - pos[0]).toFloat().pow(2) + (y - pos[1]).toFloat().pow(2)
                if (minDist == null || sqDist < minDist) {
                    minDist = sqDist
                    minX = x
                    minY = y
                }
            }
        }
        return arrayOf(minX!!, minY!!)
    }

    override fun getPrologContent(): String {
        return WasteServiceStatus(trolleyPos, error).toString()
    }
}