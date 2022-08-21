package it.unibo

import it.unibo.kactor.*
import it.unibo.kactor.MsgUtil.buildEvent
import it.unibo.kactor.MsgUtil.buildRequest
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import unibo.comm22.coap.CoapConnection
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


class TestSonarStop {
    companion object {
        const val TROLLEY_ACTOR_NAME = "trolley"
        const val WASTESERVICE_ACTOR_NAME = "wasteservice"
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"

        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "localhost",  "TCP", "8050").
            qactor( sonar_interrupt, ctx_wasteservice_proto_ctx, "it.unibo.sonar_interrupt.Sonar_interrupt").
            qactor( trolley, ctx_wasteservice_proto_ctx, "it.unibo.trolley.Trolley").
            """

        const val SONAR_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"
        const val SONAR_ACTOR_NAME = "sonar_interrupt"
        const val SONAR_HOST = "localhost"
        const val SONAR_PORT = 8050
    }

    lateinit var wasteserviceCtx: QakContext

    @Before
    fun up() {
        CommSystemConfig.tracing = false

        thread { runBlocking {
            ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, "sysRules.pl")
        } }

        waitForContexts()

    }

    @Test
    fun sonarTest() {
        ColorsOut.outappl("Starting sonar test", ColorsOut.CYAN)
        trolleyRequest("trolleyMove", "10,10")
        sonarEvent("sonarStop", "_")
        CommUtils.delay(500)
        checkSonarResponse("stopped")
        sonarEvent("sonarResume", "_")
        CommUtils.delay(500)
        checkSonarResponse("work")
    }

    private fun trolleyRequest(id: String, params: String) {
        val request = buildRequest(
            "test", id,
            "$id($params)",
            "trolley"
        ).toString()
        var reply: String? = null
        try {
            val connTcp = ConnTcp("localhost", TestDeposit.CTX_PORT)
            ColorsOut.outappl("Asking trolley: $id($params)", ColorsOut.CYAN)
            reply = connTcp.request(request)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (reply != null && reply.contains("false")) {
            Assert.fail("Trolley request <$request> failed!")
        }
    }

    private fun sonarEvent(id: String, params: String) {
        val event = buildEvent(
            "test", id,
            "$id($params)"
        ).toString()
        try {
            val connTcp = ConnTcp("localhost", TestDeposit.CTX_PORT)
            ColorsOut.outappl("Sending event: $id($params)", ColorsOut.CYAN)
            connTcp.forward(event)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkSonarResponse(expectedLedState: String) {
        val reply = coapRequest("trolley")!!
        val state = getSonarStateFromReply(reply)
        assertEquals(expectedLedState, state)
    }

    private fun waitForContexts() {
        ColorsOut.outappl(this.javaClass.name + " waits for WasteService ... ", ColorsOut.GREEN)
        waitForActor(SONAR_ACTOR_NAME)
        waitForActor(TROLLEY_ACTOR_NAME)
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

    private fun coapRequest(actor: String): String? {
        val reqConn = CoapConnection("$SONAR_HOST:$SONAR_PORT", "$SONAR_CONTEXT_NAME/$actor")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    private fun getSonarStateFromReply(reply: String): String {
        val line = it.unibo.lenziguerra.wasteservice.utils.PrologUtils.getFuncLine(reply, "state")!!;
        return it.unibo.lenziguerra.wasteservice.utils.PrologUtils.extractPayload(line)[0]
    }

}