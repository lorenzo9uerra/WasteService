package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
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
    private var actor_trolley = "trolley"


    @Before
    fun up() {
        thread { RunCtxTestDepositKt().main() }
        waitForTrolley()
        waitForWasteService()
    }

    @After
    fun down() {
        ColorsOut.outappl(this.javaClass.name + " TEST END", ColorsOut.GREEN)
    }

    @Test
    fun testDeposit() {
        wasteServiceRequest("triggerDeposit", "glass, 10")
        val wasteServiceContent = coapRequest(actor_wasteservice)?.let { PrologUtils.getFuncLine(it, "tpos") }
        val tContentParams = wasteServiceContent?.let { SimplePayloadExtractor("tpos").extractPayload(it) }
        AssertionErrors.assertEquals("Testing Expected Position", "glass_box", tContentParams?.get(0))
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
            connTcp.close()
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

    private fun waitForTrolley() {
        ColorsOut.outappl(this.javaClass.name + " waits for trolley ... ", ColorsOut.GREEN)
        var trolley = QakContext.getActor(actor_trolley)
        while (trolley == null) {
            CommUtils.delay(200)
            trolley = QakContext.getActor(actor_trolley)
        }
        ColorsOut.outappl("Trolley loaded", ColorsOut.GREEN)
    }

    private fun coapRequest(actor: String): String? {
        val reqConn = CoapConnection("$CTX_HOST:$CTX_PORT", "$CTX_TEST/$actor")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    companion object {
        var CTX_HOST = SystemConfig.hosts["wasteServiceContext"]!!
        var CTX_PORT = SystemConfig.ports["wasteServiceContext"]!!
        const val CTX_TEST = "ctx_wasteservice"
    }
}