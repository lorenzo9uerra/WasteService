package it.unibo.lenziguerra.wasteservice.led

import it.unibo.lenziguerra.wasteservice.BlinkLedState
import it.unibo.radarSystem22.domain.DeviceFactory
import it.unibo.radarSystem22.domain.interfaces.ILed
import unibo.actor22comm.utils.ColorsOut
import unibo.actor22comm.utils.CommUtils
import kotlin.concurrent.thread

class BlinkLed(val led: ILed) {
    var updateHandler: ((BlinkLedState) -> Unit)? = null
        set(value) {
            ColorsOut.out("BlinkLed: set new updateHandler!", ColorsOut.BLACK)
            field = value
        }

    var status: BlinkLedState = BlinkLedState.ON
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
                            led.turnOff()
                            CommUtils.delay(500)
                        }
                    }
                }
                updateHandler?.let { it(value) }
            }
        }

    fun turnOn() {
        status = BlinkLedState.ON
    }

    fun turnOff() {
        status = BlinkLedState.OFF
    }

    fun blink() {
        status = BlinkLedState.BLINKING
    }
}