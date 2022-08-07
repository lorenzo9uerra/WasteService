package it.unibo

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import unibo.actor22comm.coap.CoapConnection
import unibo.actor22comm.utils.ColorsOut
import unibo.actor22comm.utils.CommSystemConfig
import unibo.actor22comm.utils.CommUtils
import java.awt.Color
import kotlin.concurrent.thread


class TestLed {
    companion object {
        const val TROLLEY_ACTOR_NAME = "led_trolley"
        const val TROLLEY_CONTEXT_NAME = "ctxreq_led"

        const val LED_CONTEXT_NAME = "ctxreq_led"
        const val LED_ACTOR_NAME = "led_led"
        const val LED_HOST = "localhost"
        const val LED_PORT = 8050
    }

    lateinit var wasteserviceCtx: QakContext

    @Before
    fun up() {
        CommSystemConfig.tracing = false

        thread { it.unibo.ctxreq_led.main() }

        waitForContexts()

        replaceWatchedComponent()
    }

    @Test
    fun ledTest() {
        ColorsOut.outappl("Starting led test", ColorsOut.CYAN)

        checkLedResponse("home", "on")
        checkLedResponse("work", "blinking")
        checkLedResponse("stopped", "off")
    }

    private fun checkLedResponse(input: String, expectedLedState: String) {
        sendTrolleyInfo(input)
        CommUtils.delay(50)
        val reply = coapRequest(LED_ACTOR_NAME)!!
        val state = getLedStateFromReply(reply)
        assertEquals(expectedLedState, state)
    }


    // info: home | work | stopped
    private fun sendTrolleyInfo(info: String) {
        try {
            val connTcp = ConnTcp(LED_HOST, LED_PORT)
            connTcp.forward(MsgUtil.buildEvent(
                "test",
                "trolleyStatus",
                "trolleyStatus($info)",
            ).toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun waitForContexts() {
        ColorsOut.outappl(this.javaClass.name + " waits for WasteService ... ", ColorsOut.GREEN)
        var waitingActor = QakContext.getActor(TROLLEY_ACTOR_NAME)
        while (waitingActor == null) {
            CommUtils.delay(200)
            waitingActor = QakContext.getActor(TROLLEY_ACTOR_NAME)
        }
        ColorsOut.outappl("WasteService loaded", ColorsOut.GREEN)

        wasteserviceCtx = sysUtil.getContext(TROLLEY_CONTEXT_NAME)!!
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private fun replaceWatchedComponent() {
        // Rimuovi attori emettitori/osservati, crea falsi attori
        // o componenti software controllati da noi
        wasteserviceCtx.removeInternalActor(QakContext.getActor(TROLLEY_ACTOR_NAME)!!)
    }

    private fun coapRequest(actor: String): String? {
        val reqConn = CoapConnection("$LED_HOST:$LED_PORT", "$LED_CONTEXT_NAME/$actor")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    private fun getLedStateFromReply(reply: String): String {
        return reply
    }
}