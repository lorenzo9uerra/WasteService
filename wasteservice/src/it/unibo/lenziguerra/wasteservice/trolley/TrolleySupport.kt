package it.unibo.lenziguerra.wasteservice.trolley

import it.unibo.lenziguerra.wasteservice.utils.ApplData
import it.unibo.lenziguerra.wasteservice.utils.requestSynch

interface ITrolleySupport {
    fun init()
    fun move(location: String): Boolean
    fun deposit(): Boolean
    fun collect(): Boolean
}

object TrolleySupport {
    fun getSupport(): ITrolleySupport {
        return TrolleySupportMock()
    }
}

abstract class AbstractTrolleyVirtual(private val coords: Map<String, Array<Int>>) : ITrolleySupport {
    private var position = arrayOf<Int>(0, 0)
    private var direction = "down"

    private fun rotateTo(dir: String) {
        if (dir != direction) {
            requestSynch(ApplData.turnLeft(1000))
            when (direction) {
                "up" -> direction = "left"
                "left" -> direction = "down"
                "down" -> direction = "right"
                "right" -> direction = "up"
            }
        }
    }

    private fun changeDir(dest: Array<Int>): String {
        return if (dest[0] < position[0] && dest[1] < position[1]) {
            rotateTo("up")
            "up"
        } else if (dest[0] < position[0] && dest[1] > position[1]) {
            rotateTo("left")
            "left"
        } else if (dest[0] > position[0] && dest[1] > position[1]) {
            rotateTo("down")
            "down"
        } else {
            rotateTo("right")
            "right"
        }
    }

    override fun move(location: String): Boolean {
        val dest = coords[location]
        when (dest?.let { changeDir(it) }) {
            "up" -> {
                while (position[1] > dest[1]) {
                    requestSynch(ApplData.moveForward(1000))
                    position[1]--
                }
                requestSynch(ApplData.turnLeft(1000))
                while (position[0] > dest[0]) {
                    requestSynch(ApplData.moveForward(1000))
                    position[0]--
                }
                return true
            }
            "left" -> {
                while (position[0] > dest[0]) {
                    requestSynch(ApplData.moveForward(1000))
                    position[0]--
                }
                requestSynch(ApplData.turnLeft(1000))
                while (position[1] < dest[1]) {
                    requestSynch(ApplData.moveForward(1000))
                    position[1]++
                }
                return true
            }
            "down" -> {
                while (position[1] < dest[1]) {
                    requestSynch(ApplData.moveForward(1000))
                    position[1]++
                }
                requestSynch(ApplData.turnLeft(1000))
                while (position[0] < dest[0]) {
                    requestSynch(ApplData.moveForward(1000))
                    position[0]++
                }
                return true
            }
            "right" -> {
                while (position[0] < dest[0]) {
                    requestSynch(ApplData.moveForward(1000))
                    position[0]++
                }
                requestSynch(ApplData.turnLeft(1000))
                while (position[1] > dest[1]) {
                    requestSynch(ApplData.moveForward(1000))
                    position[1]--
                }
                return true
            }
        }
        return false
    }

    override fun deposit(): Boolean {
        TODO("Not yet implemented")
    }

    override fun collect(): Boolean {
        TODO("Not yet implemented")
    }
}