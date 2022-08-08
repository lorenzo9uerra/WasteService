package it.unibo

import alice.tuprolog.Prolog
import it.unibo.kactor.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import unibo.comm22.coap.CoapConnection
import java.awt.Color
import kotlin.concurrent.thread


class TestLed {
    companion object {
        const val TRIGGER_ACTOR_NAME = "wastetruck"
        const val TROLLEY_ACTOR_NAME = "trolley"
        const val WASTESERVICE_ACTOR_NAME = "wasteservice"
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"

        const val LED_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"
        const val LED_ACTOR_NAME = "led"
        const val LED_HOST = "localhost"
        const val LED_PORT = 8050
    }

    lateinit var wasteserviceCtx: QakContext
    lateinit var wasteserviceDummyActor: ActorBasic
    lateinit var trolleyDummyActor: ActorBasic

    @Before
    fun up() {
        CommSystemConfig.tracing = false

        thread { it.unibo.ctx_wasteservice_proto_ctx.main() }

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
        CommUtils.delay(500)
        val reply = coapRequest(LED_ACTOR_NAME)!!
        val state = getLedStateFromReply(reply)
        assertEquals(expectedLedState, state)
    }


    // info: home | work | stopped
    private fun sendTrolleyInfo(info: String) {
        if (info == "home") {
            wasteserviceDummyActor.updateResourceRep("tpos(home)")
            ColorsOut.outappl("Faked wasteservice update: tpos(home)", ColorsOut.CYAN)
        } else {
            // Make "trolley" not at home so the status it not replaced by home
            wasteserviceDummyActor.updateResourceRep("tpos(indoor)")

            trolleyDummyActor.updateResourceRep("state($info)\npos(-1,-1)")
            ColorsOut.outappl("Faked trolley update: state($info)\npos(-1,-1) time: ${System.currentTimeMillis()}", ColorsOut.CYAN)
        }
    }

    private fun waitForContexts() {
        ColorsOut.outappl(this.javaClass.name + " waits for WasteService ... ", ColorsOut.GREEN)
        var waitingActor = QakContext.getActor(WASTESERVICE_ACTOR_NAME)
        while (waitingActor == null) {
            CommUtils.delay(200)
            waitingActor = QakContext.getActor(WASTESERVICE_ACTOR_NAME)
        }
        waitingActor = QakContext.getActor(TRIGGER_ACTOR_NAME)
        while (waitingActor == null) {
            CommUtils.delay(200)
            waitingActor = QakContext.getActor(TRIGGER_ACTOR_NAME)
        }
        waitingActor = QakContext.getActor(TROLLEY_ACTOR_NAME)
        while (waitingActor == null) {
            CommUtils.delay(200)
            waitingActor = QakContext.getActor(TROLLEY_ACTOR_NAME)
        }
        ColorsOut.outappl("WasteService loaded", ColorsOut.GREEN)

        wasteserviceCtx = sysUtil.getContext(TEST_CONTEXT_NAME)!!
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private fun replaceWatchedComponent() {
        // Rimuovi attori emettitori/osservati, crea falsi attori
        // o componenti software controllati da noi
        wasteserviceCtx.removeInternalActor(QakContext.getActor(TRIGGER_ACTOR_NAME)!!)
        wasteserviceCtx.removeInternalActor(QakContext.getActor(TROLLEY_ACTOR_NAME)!!)
        wasteserviceCtx.removeInternalActor(QakContext.getActor(WASTESERVICE_ACTOR_NAME)!!)

        trolleyDummyActor = DummyActor(TROLLEY_ACTOR_NAME)
        wasteserviceDummyActor = DummyActor(WASTESERVICE_ACTOR_NAME)

        wasteserviceCtx.addActor(trolleyDummyActor)
        wasteserviceCtx.addActor(wasteserviceDummyActor)
        ColorsOut.outappl("WasteService: added fake actors <${trolleyDummyActor.name}> <${wasteserviceDummyActor.name}>", ColorsOut.GREEN)
    }

    private fun coapRequest(actor: String): String? {
        val reqConn = CoapConnection("$LED_HOST:$LED_PORT", "$LED_CONTEXT_NAME/$actor")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer time: ${System.currentTimeMillis()}", ColorsOut.CYAN)
        return answer
    }

    private fun getLedStateFromReply(reply: String): String {
        return it.unibo.lenziguerra.wasteservice.utils.PrologUtils.extractPayload(reply)[0]
    }

    internal class DummyActor(name: String) : ActorBasic(name) {
        override suspend fun actorBody(msg: IApplMessage) {
        }
    }
}