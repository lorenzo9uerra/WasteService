package it.unibo.lenziguerra.wasteservice.trolley

import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.data.TrolleyStatus
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommUtils

interface ITrolleySupport {
    fun init()
    fun preparePath(x: Int, y: Int): String
    fun prepareRotation(targetDirection: String): String

    /**
     * Confirm last path prepared with preparePath or prepareRotation
     * and apply position and direction changes
     */
    fun applyPath()
    fun getPrologContent(): String
    suspend fun collect(material: String, quantity: Float)
    suspend fun deposit()
    fun getMaterial(): String
    fun getQuantity(): String
    fun updateState(newState: String)

    // Debug / init
    fun setPosition(x: Int, y: Int)
    fun setRotation(direction: String)
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
    private var material: WasteType? = null

    private var state = TrolleyStatus.State.WORK

    private lateinit var stagedDirection: String
    private lateinit var stagedPosition: Array<Int>

    override fun setPosition(x: Int, y: Int) {
        position = arrayOf(x, y)
    }

    override fun setRotation(direction: String) {
        this.direction = direction
    }

    override fun getMaterial(): String {
        return material?.name?.lowercase() ?: ""
    }

    override fun getQuantity(): String {
        return quantity.toString()
    }

    override suspend fun collect(material: String, quantity: Float) {
        val enumMaterial = WasteType.valueOf(material.uppercase())
        if (doCollect(enumMaterial, quantity)) {
            this.material = enumMaterial
            this.quantity = quantity
        }
    }

    override suspend fun deposit() {
        if (doDeposit(this.material!!, this.quantity)) {
            this.material = null
            this.quantity = 0.0f
        }
    }

    abstract suspend fun doCollect(material: WasteType, quantity: Float): Boolean

    abstract suspend fun doDeposit(material: WasteType, quantity: Float): Boolean

    private fun rotateTo(dir: String): String {
        var cmd = ""
        while (dir != stagedDirection) {
            cmd += "l"
            when (stagedDirection) {
                "up" -> stagedDirection = "left"
                "left" -> stagedDirection = "down"
                "down" -> stagedDirection = "right"
                "right" -> stagedDirection = "up"
            }
        }
        return cmd
    }

    /**
     * Changes stagedDirection to rotate to target dir
     * @return cmd to rotate to target dir
     */
    private fun changeDir(dest: Array<Int>): String {
        if (dest[0] != stagedPosition[0] || dest[1] != stagedPosition[1]) {
            // Alto a sx
            return if (dest[0] <= stagedPosition[0] && dest[1] <= stagedPosition[1]) {
                rotateTo("up")
            // Basse a sx
            } else if (dest[0] <= stagedPosition[0]) {
                rotateTo("left")
            // Basso a dx
            } else if (dest[1] >= stagedPosition[1]) {
                rotateTo("down")
            // Alto a dx
            } else {
                rotateTo("right")
            }
        }
        return ""
    }

    override fun preparePath(x: Int, y: Int): String {
        val dest = arrayOf(x, y)

        stagedDirection = direction
        stagedPosition = position.clone()

        ColorsOut.outappl(
            "Have to go at " + dest[0] + "-" + dest[1] + "\nCurrently at: " + stagedPosition[0] + "-" + stagedPosition[1],
            ColorsOut.CYAN
        )
        // Initial rotation, side effect: change stagedDirection
        var command = changeDir(dest)

        when (stagedDirection) {
            "up" -> {
                while (stagedPosition[1] > dest[1]) {
                    command += "w"
                    stagedPosition[1]--
                }
                if (dest[0] != stagedPosition[0] || dest[1] != stagedPosition[1]) {
                    command += "l"
                    stagedDirection = "left"
                }
                while (stagedPosition[0] > dest[0]) {
                    command += "w"
                    stagedPosition[0]--
                }
            }

            "left" -> {
                while (stagedPosition[0] > dest[0]) {
                    command += "w"
                    stagedPosition[0]--
                }
                if (dest[0] != stagedPosition[0] || dest[1] != stagedPosition[1]) {
                    command += "l"
                    stagedDirection = "down"
                }
                while (stagedPosition[1] < dest[1]) {
                    command += "w"
                    stagedPosition[1]++
                }
            }

            "down" -> {
                while (stagedPosition[1] < dest[1]) {
                    command += "w"
                    stagedPosition[1]++
                }
                if (dest[0] != stagedPosition[0] || dest[1] != stagedPosition[1]) {
                    command += "l"
                    stagedDirection = "right"
                }
                while (stagedPosition[0] < dest[0]) {
                    command += "w"
                    stagedPosition[0]++
                }
            }

            "right" -> {
                while (stagedPosition[0] < dest[0]) {
                    command += "w"
                    stagedPosition[0]++
                }
                if (dest[0] != stagedPosition[0] || dest[1] != stagedPosition[1]) {
                    command += "l"
                    stagedDirection = "up"
                }
                while (stagedPosition[1] > dest[1]) {
                    command += "w"
                    stagedPosition[1]--
                }

            }

            else -> throw java.lang.IllegalStateException("Cannot reach $x, $y from $position")
        }

        // Sostituisci giri con niente
        command = command.replace("llll", "")
        // Sostituisci sx di 270° con dx di 90°
        command = command.replace("lll", "r")

        return command
    }

    override fun prepareRotation(targetDirection: String): String {
        stagedPosition = position.clone()
        stagedDirection = direction

        return rotateTo(targetDirection)
    }

    override fun applyPath() {
        position = stagedPosition.clone()
        direction = stagedDirection
    }

    override fun updateState(newState: String) {
        state = TrolleyStatus.State.valueOf(newState.uppercase())
    }

    override fun getPrologContent(): String {
        return TrolleyStatus(state, position, material, quantity).toString()
    }

    override fun toString(): String {
        return "Trolley | Pos: (${position[0]},${position[1]}), Dir: $direction, State: $state" +
                (material?.let { ", Content: $quantity $material" } ?: "")
    }
}
