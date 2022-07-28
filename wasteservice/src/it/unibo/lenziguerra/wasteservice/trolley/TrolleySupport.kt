package it.unibo.lenziguerra.wasteservice.trolley

import it.unibo.lenziguerra.wasteservice.utils.ApplData
import it.unibo.lenziguerra.wasteservice.utils.requestSynch
import unibo.actor22comm.utils.ColorsOut

interface ITrolleySupport {
    fun init()
    fun move(strdest: String): Boolean
    fun getPrologContent(): String
    fun collect(material: String, quantity: Float)
    fun getMaterial(): String
    fun getQuantity(): String
    fun setPosition(pos: String)
}

object TrolleySupport {
    fun getSupport(): ITrolleySupport {
        return TrolleySupportMock()
    }
}

abstract class AbstractTrolleyVirtual : ITrolleySupport {
    private var position = arrayOf(0, 0)
    private var direction = "down"
    private var quantity = 0.0f
    private var material = ""

    override fun setPosition(pos: String) {
        position = arrayOf(
            Regex("x(.*)y").find(pos)!!.destructured.component1().toInt(),
            Regex("y(.*)$").find(pos)!!.destructured.component1().toInt()
        )
    }

    override fun getMaterial(): String {
        return material
    }

    override fun getQuantity(): String {
        return quantity.toString()
    }

    private fun rotateTo(dir: String): String {
        while (dir != direction) {
            requestSynch(ApplData.turnLeft(500))
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
            if (dest[0] <= position[0] && dest[1] <= position[1]) {
                rotateTo("up")
            } else if (dest[0] <= position[0]) {
                rotateTo("left")
            } else if (dest[1] >= position[1]) {
                rotateTo("down")
            } else {
                rotateTo("right")
            }
        }
        return direction
    }

    override fun collect(material: String, quantity: Float) {
        this.material = material
        this.quantity = quantity
    }

    override fun move(strdest: String): Boolean {
        val dest = arrayOf(
            Regex("x(.*)y").find(strdest)!!.destructured.component1().toInt(),
            Regex("y(.*)$").find(strdest)!!.destructured.component1().toInt()
        )

        ColorsOut.outappl(
            "Have to go at " + dest[0] + "-" + dest[1] + "\nCurrently at: " + position[0] + "-" + position[1],
            ColorsOut.CYAN
        )
        when (changeDir(dest)) {
            "up" -> {
                while (position[1] > dest[1]) {
                    requestSynch(ApplData.moveForward(500))
                    position[1]--
                }
                if (dest[0] != position[0] || dest[1] != position[1]) {
                    requestSynch(ApplData.turnLeft(500))
                    direction = "left"
                }
                while (position[0] > dest[0]) {
                    requestSynch(ApplData.moveForward(500))
                    position[0]--
                }
                return true
            }
            "left" -> {
                while (position[0] > dest[0]) {
                    requestSynch(ApplData.moveForward(500))
                    position[0]--
                }
                if (dest[0] != position[0] || dest[1] != position[1]) {
                    requestSynch(ApplData.turnLeft(500))
                    direction = "down"
                }
                while (position[1] < dest[1]) {
                    requestSynch(ApplData.moveForward(500))
                    position[1]++
                }
                return true
            }
            "down" -> {
                while (position[1] < dest[1]) {
                    requestSynch(ApplData.moveForward(500))
                    position[1]++
                }
                if (dest[0] != position[0] || dest[1] != position[1]) {
                    requestSynch(ApplData.turnLeft(500))
                    direction = "right"
                }
                while (position[0] < dest[0]) {
                    requestSynch(ApplData.moveForward(500))
                    position[0]++
                }
                return true
            }
            "right" -> {
                while (position[0] < dest[0]) {
                    requestSynch(ApplData.moveForward(500))
                    position[0]++
                }
                if (dest[0] != position[0] || dest[1] != position[1]) {
                    requestSynch(ApplData.turnLeft(500))
                    direction = "up"
                }
                while (position[1] > dest[1]) {
                    requestSynch(ApplData.moveForward(500))
                    position[1]--
                }
                return true
            }
        }
        return false
    }

    override fun getPrologContent(): String {
        return "state(idle) $position,$quantity,$material"
    }
}
