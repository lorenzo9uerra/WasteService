package it.unibo

import it.unibo.kactor.*
import it.unibo.kactor.MsgUtil.buildEvent
import it.unibo.kactor.MsgUtil.buildRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import unibo.comm22.coap.CoapConnection
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import java.time.LocalTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class TestSonarStop {
    companion object {
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"

        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "localhost",  "TCP", "8050").
            qactor( sonarinterrupter, $TEST_CONTEXT_NAME, "it.unibo.sonarinterrupter.Sonarinterrupter").
            qactor( trolley, $TEST_CONTEXT_NAME, "it.unibo.trolley.Trolley").
            qactor( pathexecws, $TEST_CONTEXT_NAME, "it.unibo.pathexecws.Pathexecws").
            qactor( timer, $TEST_CONTEXT_NAME, "it.unibo.timer.Timer").
            """

        val contexts = mapOf(
            "trolley" to "ctx_wasteservice_proto_ctx",
            "sonarinterrupter" to "ctx_wasteservice_proto_ctx",
        )
        val actors = mapOf(
            "trolley" to "trolley",
            "sonarinterrupter" to "sonarinterrupter",
        )
        val hosts = mapOf(
            "trolley" to "localhost",
            "sonarinterrupter" to "localhost",
        )
        val ports = mapOf(
            "trolley" to 8050,
            "sonarinterrupter" to 8050,
        )
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
        val trolleySuccess = trolleyRequestAsync("trolleyMove", "10,10")
        sonarEvent("sonarDistance", "0")
        CommUtils.delay(500)
        checkTrolleyState("stopped")
        sonarEvent("sonarDistance", "200")
        CommUtils.delay(500)
        checkTrolleyState("work")
        assertTrue(trolleySuccess.get(1000, TimeUnit.MILLISECONDS))
    }

    private fun trolleyRequestAsync(id: String, params: String): Future<Boolean> {
        val request = buildRequest(
            "test", id,
            "$id($params)",
            "trolley"
        ).toString()
        val connTcp = ConnTcp("localhost", TestDeposit.CTX_PORT)
        ColorsOut.outappl("Asking trolley: $id($params)", ColorsOut.CYAN)
        connTcp.forward(request)

        val future = CompletableFuture<Boolean>()

        thread {
            val reply = connTcp.receiveMsg()
            future.complete(reply != null && !reply.contains("false"))
        }

        return future
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

    private fun checkTrolleyState(expectedTrolleyState: String) {
        var reply = coapRequest("trolley")
        var state = getTrolleyStateFromReply(reply)
        // state null: non ancora impostato la prima volta
        while (state == null) {
            CommUtils.delay(200)
            reply = coapRequest("trolley")
            state = getTrolleyStateFromReply(reply)
        }
        assertEquals(expectedTrolleyState, state)
    }

    private fun waitForContexts() {
        ColorsOut.outappl(this.javaClass.name + " waits for WasteService ... ", ColorsOut.GREEN)
        waitForActor(actors["sonarinterrupter"]!!)
        waitForActor(actors["trolley"]!!)
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

    private fun coapRequest(actor: String): String {
        val reqConn = CoapConnection("${hosts[actor]}:${ports[actor]}", "${contexts[actor]}/${actors[actor]}")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    private fun getTrolleyStateFromReply(reply: String): String? {
        return it.unibo.lenziguerra.wasteservice.utils.PrologUtils.getFuncLine(reply, "state")?.let { line ->
            it.unibo.lenziguerra.wasteservice.utils.PrologUtils.extractPayload(line)[0]
        }
    }

}