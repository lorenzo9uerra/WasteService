package it.unibo.lenziguerra.wasteservice.led

import it.unibo.lenziguerra.wasteservice.BlinkLedState
import it.unibo.radarSystem22.domain.DeviceFactory
import it.unibo.radarSystem22.domain.interfaces.ILed
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommUtils
import kotlin.concurrent.thread

interface IBlinkLed {
    fun turnOn()
    fun turnOff()
    fun blink()

    val status: BlinkLedState
    var updateHandler: ((BlinkLedState) -> Unit)?
}


class BlinkLed(val led: ILed) : IBlinkLed {
    override var updateHandler: ((BlinkLedState) -> Unit)? = null
        set(value) {
            ColorsOut.out("BlinkLed: set new updateHandler!", ColorsOut.BLACK)
            field = value
        }

    override var status: BlinkLedState = BlinkLedState.OFF
        get() = field
        set(value) {
            if (value != field) {
                ColorsOut.out("Setting BlinkLed to $value", ColorsOut.CYAN)
                field = value
                when (value) {
                    BlinkLedState.ON -> led.turnOn()
                    BlinkLedState.OFF -> led.turnOff()
                    BlinkLedState.BLINKING -> thread {
                        while (status == BlinkLedState.BLINKING) {
                            led.turnOn()
                            CommUtils.delay(500)
                            if (status == BlinkLedState.BLINKING)
                                led.turnOff()
                            CommUtils.delay(500)
                        }
                    }
                }
                updateHandler?.let { it(value) }
            }
        }

    override fun turnOn() {
        status = BlinkLedState.ON
    }

    override fun turnOff() {
        status = BlinkLedState.OFF
    }

    override fun blink() {
        status = BlinkLedState.BLINKING
    }
}