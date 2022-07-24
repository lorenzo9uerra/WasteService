package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.test.util.AssertionErrors
import unibo.actor22comm.coap.CoapConnection
import unibo.actor22comm.utils.ColorsOut
import unibo.actor22comm.utils.CommUtils
import kotlin.concurrent.thread

class WasteServiceTest {
    private var actor_wasteservice = "wasteservice"


    @Before
    fun up() {
        thread { RunTestWasteServiceKt().main() }
        waitForWasteService()
    }

    @After
    fun down() {
        ColorsOut.outappl(this.javaClass.name + " TEST END", ColorsOut.GREEN)
    }

    @Test
    fun testDeposit() {
        wasteServiceDispatch("loadDeposit", "glass, 10")
        CommUtils.delay(5000)
        wasteServiceRequest("finishLoad", "")
    }

    private fun wasteServiceRequest(id: String, params: String) {
        val request: String = MsgUtil.buildRequest(
            "test", id, "$id($params)", actor_wasteservice
        ).toString()
        var reply: String? = null
        try {
            val connTcp = ConnTcp("localhost", CTX_PORT)
            ColorsOut.outappl("Asking wasteservice: $id($params)", ColorsOut.CYAN)
            reply = connTcp.request(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (reply != null && reply.contains("false")) {
            AssertionErrors.fail("Trolley request <$request> failed!")
        }
    }

    private fun wasteServiceDispatch(id: String, params: String) {
        val message: String = MsgUtil.buildDispatch("test", id, "$id($params)", actor_wasteservice).toString()
        try {
            val connTcp = ConnTcp("localhost", CTX_PORT)
            ColorsOut.outappl("Sending dispatch to wasteservice: $id($params)", ColorsOut.CYAN)
            connTcp.forward(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun waitForWasteService() {
        ColorsOut.outappl(this.javaClass.name + " waits for wasteservice ... ", ColorsOut.GREEN)
        var wasteservice = QakContext.getActor(actor_wasteservice)
        while (wasteservice == null) {
            CommUtils.delay(200)
            wasteservice = QakContext.getActor(actor_wasteservice)
        }
        ColorsOut.outappl("WasteService loaded", ColorsOut.GREEN)
    }

    private fun coapRequest(actor: String): String? {
        val reqConn = CoapConnection("$CTX_HOST:$CTX_PORT", "$CTX_TEST/$actor")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    companion object {
        const val CTX_HOST = "localhost"
        const val CTX_PORT = 8023
        const val CTX_TEST = "ctx_wasteservice"
    }
}