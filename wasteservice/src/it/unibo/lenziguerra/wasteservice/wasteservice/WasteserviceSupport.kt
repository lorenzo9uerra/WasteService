package it.unibo.lenziguerra.wasteservice.wasteservice

import it.unibo.lenziguerra.wasteservice.SystemConfig
import kotlin.math.abs

interface IWasteserviceSupport {
    fun getDestination(location: String, Position: String): String
}

class WasteserviceSupport : IWasteserviceSupport {
    override fun getDestination(location: String, Position: String): String {
        val pos = arrayOf(
            Position.split(",")[0].toInt(),
            Position.split(",")[1].toInt(),
        )
        val destarea = SystemConfig.positions[location] ?: throw IllegalArgumentException("Unknown location $location")
        var x = destarea[0][0]
        var y = destarea[0][1]
        var minX = 10000
        var minY = 10000
        for (i in destarea[0][0]..destarea[1][0]) {
            if (abs(pos[0] - i) < minX) {
                x = i
                minX = abs(pos[0] - 1)
            }
        }
        for (i in destarea[0][1]..destarea[1][1]) {
            if (abs(pos[1] - i) < minY) {
                y = i
                minY = abs(pos[1] - 1)
            }
        }
        return "${x},$y"
    }
}