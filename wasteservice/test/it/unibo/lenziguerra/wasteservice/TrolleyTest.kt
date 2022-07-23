package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext.Companion.getActor
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.test.util.AssertionErrors.assertEquals
import org.springframework.test.util.AssertionErrors.fail
import unibo.actor22comm.coap.CoapConnection
import unibo.actor22comm.utils.ColorsOut
import unibo.actor22comm.utils.CommUtils
import kotlin.concurrent.thread


class TrolleyTest() {
    private var actor_trolley = "trolley"
    private var trolleyPosObserver: TrolleyPosObserver? = null


    @Before
    fun up() {
        thread { RunTestTrolleyKt().main() }
        waitForTrolley()
        startTrolleyCoapConnection()
    }

    @After
    fun down() {
        ColorsOut.outappl(this.javaClass.name + " TEST END", ColorsOut.GREEN)
    }

    @Test
    fun testTrolleyMove() {
        trolleyRequest("trolleyMove", "INDOOR")
        val trolleyContent = coapRequest(actor_trolley)?.let { PrologUtils.getFuncLine(it, "pos") }
        val tContentParams = trolleyContent?.let { SimplePayloadExtractor("pos").extractPayload(it) }
        assertEquals("Testing Expected Position", "INDOOR", tContentParams?.get(0))
    }

    @Test
    fun testTrolleyCollect() {
        trolleyRequest("trolleyCollect", "glass, 10")
        val trolleyContent = coapRequest(actor_trolley)?.let { PrologUtils.getFuncLine(it, "content") }
        val tContentParams = trolleyContent?.let { SimplePayloadExtractor("content").extractPayload(it) }
        assertEquals("Testing Correct Material", "glass", tContentParams?.get(0))
        assertEquals("Testing Correct Quantity", 10.0f, tContentParams?.get(1)?.toFloat())
    }

    @Test
    fun testTrolleyDeposit() {
        trolleyRequest("trolleyDeposit", "")
    }

    private fun trolleyRequest(id: String, params: String) {
        val request: String = MsgUtil.buildRequest(
            "test", id, "$id($params)", actor_trolley
        ).toString()
        var reply: String? = null
        try {
            val connTcp = ConnTcp("localhost", CTX_PORT)
            ColorsOut.outappl("Asking trolley: $id($params)", ColorsOut.CYAN)
            reply = connTcp.request(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (reply != null && reply.contains("false")) {
            fail("Trolley request <$request> failed!")
        }
    }

    private fun waitForTrolley() {
        ColorsOut.outappl(this.javaClass.name + " waits for trolley ... ", ColorsOut.GREEN)
        var trolley = getActor(actor_trolley)
        while (trolley == null) {
            CommUtils.delay(200)
            trolley = getActor(actor_trolley)
        }
        ColorsOut.outappl("Trolley loaded", ColorsOut.GREEN)
    }

    private fun startTrolleyCoapConnection() {
        trolleyPosObserver = TrolleyPosObserver()
        Thread {
            val conn = CoapConnection("$CTX_HOST:$CTX_PORT", "$CTX_TEST/$actor_trolley")
            conn.observeResource(trolleyPosObserver)
            ColorsOut.outappl("connected via Coap conn:$conn", ColorsOut.CYAN)
        }.start()
    }

    private fun coapRequest(actor: String): String? {
        val reqConn = CoapConnection("$CTX_HOST:$CTX_PORT", "$CTX_TEST/$actor")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    companion object {
        const val CTX_HOST = "localhost"
        const val CTX_PORT = 8022
        const val CTX_TEST = "ctx_trolley"
    }
}
