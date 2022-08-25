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
        val contexts = mapOf(
            "trolley" to "ctxreq_sonar",
        )
        val actors = mapOf(
            "trolley" to "trolley_sonar",
        )
        val hosts = mapOf(
            "trolley" to "localhost",
        )
        val ports = mapOf(
            "trolley" to 8050,
        )
    }

    lateinit var wasteserviceCtx: QakContext

    @Before
    fun up() {
        CommSystemConfig.tracing = false

        thread { it.unibo.ctxreq_sonar.main() }

        waitForContexts()
    }

    @Test
    fun sonarTest() {
        ColorsOut.outappl("Starting sonar test", ColorsOut.CYAN)
        sonarEvent("0")
        CommUtils.delay(500)
        checkTrolleyState("stopped")
        sonarEvent("200")
        CommUtils.delay(500)
        checkTrolleyState("work")
        assertTrue(trolleySuccess.get(1000, TimeUnit.MILLISECONDS))
    }

    private fun sonarEvent(distance: String) {
        // Forza la rilevazione di una certa distanza
        TODO("Update with problem analysis")
    }

    private fun checkTrolleyState(expectedTrolleyState: String) {
        var reply = coapRequest("trolley")
        var state = getTrolleyStateFromReply(reply)
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
        return reply
    }

}