package it.unibo.lenziguerra.wasteservice.trolley

import it.unibo.lenziguerra.wasteservice.utils.sendRequest
import unibo.actor22comm.utils.ColorsOut

interface ITrolleySupport {
    fun init()
    fun move(strdest: String): Boolean
    fun getPrologContent(): String
    fun collect(material: String, quantity: Float)
    fun deposit()
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
    private var quantity: Float = 0.0f
    private var material: String? = null

    override fun setPosition(pos: String) {
        position = arrayOf(
            Regex("x(.*)y").find(pos)!!.destructured.component1().toInt(),
            Regex("y(.*)$").find(pos)!!.destructured.component1().toInt()
        )
    }

    override fun getMaterial(): String {
        return material ?: ""
    }

    override fun getQuantity(): String {
        return quantity.toString()
    }

    private fun rotateTo(dir: String): String {
        var cmd = ""
        while (dir != direction) {
            cmd += "l"
            when (direction) {
                "up" -> direction = "left"
                "left" -> direction = "down"
                "down" -> direction = "right"
                "right" -> direction = "up"
            }
        }
        sendRequest("dopath", cmd, "pathexec")
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

    override fun deposit() {
        this.material = null
        this.quantity = 0.0f
    }

    override fun move(strdest: String): Boolean {
        var command = ""
        val posMatch = Regex("x(.*)y(.*)$").find(strdest)!!.destructured
        val dest = arrayOf(
            posMatch.component1().toInt(),
            posMatch.component2().toInt(),
        )

        ColorsOut.outappl(
            "Have to go at " + dest[0] + "-" + dest[1] + "\nCurrently at: " + position[0] + "-" + position[1],
            ColorsOut.CYAN
        )
        when (changeDir(dest)) {
            "up" -> {
                while (position[1] > dest[1]) {
                    command += "w"
                    position[1]--
                }
                if (dest[0] != position[0] || dest[1] != position[1]) {
                    command += "l"
                    direction = "left"
                }
                while (position[0] > dest[0]) {
                    command += "w"
                    position[0]--
                }
            }

            "left" -> {
                while (position[0] > dest[0]) {
                    command += "w"
                    position[0]--
                }
                if (dest[0] != position[0] || dest[1] != position[1]) {
                    command += "l"
                    direction = "down"
                }
                while (position[1] < dest[1]) {
                    command += "w"
                    position[1]++
                }
            }

            "down" -> {
                while (position[1] < dest[1]) {
                    command += "w"
                    position[1]++
                }
                if (dest[0] != position[0] || dest[1] != position[1]) {
                    command += "l"
                    direction = "right"
                }
                while (position[0] < dest[0]) {
                    command += "w"
                    position[0]++
                }
            }

            "right" -> {
                while (position[0] < dest[0]) {
                    command += "w"
                    position[0]++
                }
                if (dest[0] != position[0] || dest[1] != position[1]) {
                    command += "l"
                    direction = "up"
                }
                while (position[1] > dest[1]) {
                    command += "w"
                    position[1]--
                }

            }

            else -> return false
        }
        sendRequest("dopath", command, "pathexec")
        return true
    }

    override fun getPrologContent(): String {
        return "state(work)\npos(${position[0]},${position[1]})" +
                (material?.let { "\ncontent($material,$quantity)" } ?: "")
    }

    override fun toString(): String {
        return "Trolley | Pos: (${position[0]},${position[1]}), Dir: $direction" +
                (material?.let { ", Content: $quantity $material" } ?: "")
    }
}
