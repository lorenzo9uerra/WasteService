package it.unibo

import it.unibo.kactor.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import unibo.comm22.coap.CoapConnection
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


class TestLed {
    companion object {
        const val TROLLEY_ACTOR_NAME = "trolley"
        const val WASTESERVICE_ACTOR_NAME = "wasteservice"
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"

        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "localhost",  "TCP", "8050").
            qactor( ledcontroller, ctx_wasteservice_proto_ctx, "it.unibo.ledcontroller.Ledcontroller").
            qactor( blinkled, ctx_wasteservice_proto_ctx, "it.unibo.blinkled.Blinkled").
            """

        const val LED_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"
        const val LED_ACTOR_NAME = "blinkled"
        const val LED_HOST = "localhost"
        const val LED_PORT = 8050
    }

    lateinit var wasteserviceCtx: QakContext
    private lateinit var wasteserviceDummyActor: DummyActor
    private lateinit var trolleyDummyActor: DummyActor

    @Before
    fun up() {
        CommSystemConfig.tracing = false

        thread { runBlocking {
            ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, "sysRules.pl")
        } }

        waitForContexts()

        replaceWatchedComponent()
    }

    @Test
    fun ledTest() {
        ColorsOut.outappl("Starting led test", ColorsOut.CYAN)

        // Controlla alcuni stati più volte, per verificare
        // che l'ordine non influisca

        checkLedResponse("work", "blinking")
        checkLedResponse("stopped", "off")
        // Home/on stato iniziale, controlla alla fine per evitare
        // che sembri funzionare solo perchè è così alla partenza
        checkLedResponse("home", "on")
        checkLedResponse("stopped", "off")
        checkLedResponse("work", "blinking")
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
            wasteserviceDummyActor.fakeResourceUpdate("tpos(home)")
            // Make stopped false else it would take priority
            trolleyDummyActor.fakeResourceUpdate("state(work)\npos(-1,-1)")
        } else {
            // Make "trolley" not at home so the status it not replaced by home
            wasteserviceDummyActor.fakeResourceUpdate("tpos(indoor)")
            trolleyDummyActor.fakeResourceUpdate("state($info)\npos(-1,-1)")
        }
    }

    private fun waitForContexts() {
        ColorsOut.outappl(this.javaClass.name + " waits for WasteService ... ", ColorsOut.GREEN)
        waitForActor(LED_ACTOR_NAME)
        ColorsOut.outappl("WasteService loaded", ColorsOut.GREEN)

        wasteserviceCtx = sysUtil.getContext(TEST_CONTEXT_NAME)!!
    }

    private fun waitForActor(actor: String) {
        var waitingActor = QakContext.getActor(actor)
        while (waitingActor == null) {
            CommUtils.delay(200)
            waitingActor = QakContext.getActor(actor)
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private fun replaceWatchedComponent() {
        trolleyDummyActor = DummyActor(TROLLEY_ACTOR_NAME)
        wasteserviceDummyActor = DummyActor(WASTESERVICE_ACTOR_NAME)

        wasteserviceCtx.addActor(trolleyDummyActor)
        wasteserviceCtx.addActor(wasteserviceDummyActor)
        ColorsOut.outappl("WasteService: added fake actors <${trolleyDummyActor.name}> <${wasteserviceDummyActor.name}>", ColorsOut.GREEN)
    }

    private fun coapRequest(actor: String): String? {
        val reqConn = CoapConnection("$LED_HOST:$LED_PORT", "$LED_CONTEXT_NAME/$actor")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    private fun getLedStateFromReply(reply: String): String {
        return it.unibo.lenziguerra.wasteservice.utils.PrologUtils.extractPayload(reply)[0]
    }

    internal class DummyActor(name: String) : ActorBasic(name) {
        override suspend fun actorBody(msg: IApplMessage) {
        }

        fun fakeResourceUpdate(data: String) {
            updateResourceRep(data)
            ColorsOut.outappl("DummyActor | ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())} Faked $name update: $data", ColorsOut.ANSI_PURPLE)
        }
    }
}