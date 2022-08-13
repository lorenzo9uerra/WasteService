package it.unibo.lenziguerra.wasteservice.led

import it.unibo.kactor.ActorBasic
import it.unibo.kactor.IApplMessage
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import it.unibo.lenziguerra.wasteservice.BlinkLedState
import it.unibo.lenziguerra.wasteservice.ContextTestUtils
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.lenziguerra.wasteservice.data.LedStatus
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

        const val TEST_CONTEXT_NAME = "ctx_wasteservice_test"
        const val TEST_CONTEXT_HOST = "localhost"
        const val TEST_CONTEXT_PORT = 8050
        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "$TEST_CONTEXT_HOST",  "TCP", "$TEST_CONTEXT_PORT")."""
    }

    lateinit var wasteserviceCtx: QakContext
    private lateinit var wasteserviceDummyActor: DummyActor
    private lateinit var trolleyDummyActor: DummyActor

    private lateinit var ledContainer: LedContainer

    @Before
    fun up() {
        CommSystemConfig.tracing = false

        thread { runBlocking {
            val sysRulesResource = javaClass.classLoader.getResource("sysRules.pl")!!
            ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, sysRulesResource.file)
        } }

        waitForContexts()

        replaceWatchedComponent()

        ledContainer = LedContainer()

        setupAndStartLed()
    }

    @Test
    fun ledTest() {
        ColorsOut.outappl("Starting led test", ColorsOut.CYAN)

        // Controlla alcuni stati più volte, per verificare
        // che l'ordine non influisca

        checkLedResponse("work", BlinkLedState.BLINKING)
        checkLedResponse("stopped", BlinkLedState.OFF)
        // Home/on stato iniziale, controlla alla fine per evitare
        // che sembri funzionare solo perchè è così alla partenza
        checkLedResponse("home", BlinkLedState.ON)
        checkLedResponse("stopped", BlinkLedState.OFF)
        checkLedResponse("work", BlinkLedState.BLINKING)
    }

    private fun checkLedResponse(input: String, expectedLedState: BlinkLedState) {
        sendTrolleyInfo(input)
        CommUtils.delay(500)

        // Check both led true status and coap request
        assertEquals(expectedLedState, ledContainer.led.status)

        val reply = coapRequest()!!
        val coapState = getLedStateFromReply(reply)
        assertEquals(expectedLedState, coapState)
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

    private fun setupAndStartLed() {
        SystemConfig.hosts["trolley"] = TEST_CONTEXT_HOST
        SystemConfig.hosts["wasteServiceContext"] = TEST_CONTEXT_HOST
        SystemConfig.ports["trolley"] = TEST_CONTEXT_PORT
        SystemConfig.ports["wasteServiceContext"] = TEST_CONTEXT_PORT
        SystemConfig.contexts["trolley"] = TEST_CONTEXT_NAME
        SystemConfig.contexts["wasteServiceContext"] = TEST_CONTEXT_NAME
        SystemConfig.actors["trolley"] = TROLLEY_ACTOR_NAME
        SystemConfig.actors["wasteServiceContext"] = WASTESERVICE_ACTOR_NAME

        ledContainer.start()
    }

    private fun waitForContexts() {
        // Context starts empty, wait for it instead of actors
        var ctx = sysUtil.getContext(TEST_CONTEXT_NAME)
        while (ctx == null) {
            CommUtils.delay(200)
            ctx = sysUtil.getContext(TEST_CONTEXT_NAME)
        }
        wasteserviceCtx = ctx
        ColorsOut.outappl("WasteService loaded", ColorsOut.GREEN)

    }

    private fun replaceWatchedComponent() {
        trolleyDummyActor = DummyActor(TROLLEY_ACTOR_NAME)
        wasteserviceDummyActor = DummyActor(WASTESERVICE_ACTOR_NAME)

        wasteserviceCtx.addActor(trolleyDummyActor)
        wasteserviceCtx.addActor(wasteserviceDummyActor)
        ColorsOut.outappl("WasteService: added fake actors <${trolleyDummyActor.name}> <${wasteserviceDummyActor.name}>", ColorsOut.GREEN)
    }

    private fun coapRequest(): String? {
        val reqConn = CoapConnection("localhost:${SystemConfig.ports["led"]!!}", "led")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    private fun getLedStateFromReply(reply: String): BlinkLedState {
        return LedStatus.fromProlog(reply).state
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