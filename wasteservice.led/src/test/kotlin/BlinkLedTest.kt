package it.unibo.lenziguerra.wasteservice.led

import it.unibo.radarSystem22.domain.DeviceFactory
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import unibo.comm22.utils.CommUtils

class BlinkLedTest {
    lateinit var led: BlinkLed

    @Before
    fun up() {
        DomainSystemConfig.simulateLed = true

        led = BlinkLed(DeviceFactory.createLed())
    }

    @Test
    fun blinkLedTest() {
        led.blink()

        var changedTimes = 0;
        var duration = 3000
        val startTime = System.currentTimeMillis()
        var lastState = led.led.state

        while (System.currentTimeMillis() - startTime < duration) {
            CommUtils.delay(200)
            if (led.led.state != lastState) {
                lastState = led.led.state
                changedTimes++
            }
        }

        println("Changed $changedTimes times")
        assertTrue(changedTimes >= duration / 500 - 1 && changedTimes <= duration / 500 + 1)
    }
}