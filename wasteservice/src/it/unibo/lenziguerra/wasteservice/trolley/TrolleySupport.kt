package it.unibo.lenziguerra.wasteservice.trolley

import it.unibo.lenziguerra.wasteservice.utils.ApplData
import it.unibo.lenziguerra.wasteservice.utils.requestSynch
import unibo.actor22comm.utils.ColorsOut

interface ITrolleySupport {
    fun init()
    fun move(location: String): Boolean
}

object TrolleySupport {
    fun getSupport(): ITrolleySupport {
        return TrolleySupportMock()
    }
}

abstract class AbstractTrolleyVirtual(private val coords: Map<String, Array<Int>>) : ITrolleySupport {
    private var position = arrayOf(0, 0)
    private var direction = "down"

    private fun rotateTo(dir: String): String {
        while (dir != direction) {
            requestSynch(ApplData.turnLeft(100))
            when (direction) {
                "up" -> direction = "left"
                "left" -> direction = "down"
                "down" -> direction = "right"
                "right" -> direction = "up"
            }
        }
        return dir
    }

    private fun changeDir(dest: Array<Int>): String {
        if (dest[0] != position[0] && dest[1] != position[1]) {
            return if (dest[0] <= position[0] && dest[1] <= position[1]) {
                ColorsOut.outappl("rotating up", ColorsOut.GREEN)
                rotateTo("up")
            } else if (dest[0] <= position[0]) {
                ColorsOut.outappl("rotating left", ColorsOut.GREEN)
                rotateTo("left")
            } else if (dest[1] >= position[1]) {
                ColorsOut.outappl("rotating down", ColorsOut.GREEN)
                rotateTo("down")
            } else {
                ColorsOut.outappl("rotating right", ColorsOut.GREEN)
                rotateTo("right")
            }
        }
        return direction
    }

    override fun move(location: String): Boolean {
        val dest = coords[location]
        dest?.let {
            ColorsOut.outappl(
                "have to go " + location + ": " + dest[0] + "-" + dest[1] + "\nMa sono: " + position[0] + "-" + position[1],
                ColorsOut.GREEN
            )
        }
        when (dest?.let { changeDir(it) }) {
            "up" -> {
                while (position[1] > dest[1]) {
                    requestSynch(ApplData.moveForward(300))
                    position[1]--
                }
                if (dest[0] != position[0] || dest[1] != position[1])
                    requestSynch(ApplData.turnLeft(300))
                while (position[0] > dest[0]) {
                    requestSynch(ApplData.moveForward(300))
                    position[0]--
                }
                return true
            }
            "left" -> {
                while (position[0] > dest[0]) {
                    requestSynch(ApplData.moveForward(300))
                    position[0]--
                }
                if (dest[0] != position[0] || dest[1] != position[1])
                    requestSynch(ApplData.turnLeft(300))
                while (position[1] < dest[1]) {
                    requestSynch(ApplData.moveForward(300))
                    position[1]++
                }
                return true
            }
            "down" -> {
                while (position[1] < dest[1]) {
                    requestSynch(ApplData.moveForward(300))
                    position[1]++
                }
                if (dest[0] != position[0] || dest[1] != position[1])
                    requestSynch(ApplData.turnLeft(300))
                while (position[0] < dest[0]) {
                    requestSynch(ApplData.moveForward(300))
                    position[0]++
                }
                return true
            }
            "right" -> {
                while (position[0] < dest[0]) {
                    requestSynch(ApplData.moveForward(300))
                    position[0]++
                }
                if (dest[0] != position[0] || dest[1] != position[1])
                    requestSynch(ApplData.turnLeft(300))
                while (position[1] > dest[1]) {
                    requestSynch(ApplData.moveForward(300))
                    position[1]--
                }
                return true
            }
        }
        return false
    }
}