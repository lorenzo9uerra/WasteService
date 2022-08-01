package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext.Companion.getActor
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils.getFuncLine
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils.getFuncLines
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import unibo.actor22comm.coap.CoapConnection
import unibo.actor22comm.utils.ColorsOut
import unibo.actor22comm.utils.CommUtils
import kotlin.concurrent.thread


class TrolleyTest() {
    companion object {
        const val actor_trolley = "trolley"
        const val actor_storage = "storagemanager"
    }

    lateinit var ctx_trolley: String

    @Before
    fun up() {
        SystemConfig.setConfiguration();
        thread { RunTestTrolley().main() }
        waitForTrolley()
    }

    @After
    fun down() {
        ColorsOut.outappl(this.javaClass.name + " TEST END", ColorsOut.GREEN)
    }

    @Test
    fun testTrolleyCollect() {
        val trolleyContentBefore = getFuncLine(coapRequestTrolley(actor_trolley), "content") ?: "content(glass, 0)"

        trolleyRequest("trolleyCollect", "glass, 10")
        val trolleyContentAfter = getFuncLine(coapRequestTrolley(actor_trolley), "content")!!

        val tContentParamsBefore = PrologUtils.extractPayload(trolleyContentBefore)
        val tContentParamsAfter = PrologUtils.extractPayload(trolleyContentAfter)

        assertEquals("glass", tContentParamsAfter[0])
        assertEquals(10.0, tContentParamsAfter[1].toDouble() - tContentParamsBefore[1].toDouble(), 0.0001)
    }

    @Test
    fun testTrolleyDeposit() {
        storageDispatch("testStorageReset", "")

        trolleyRequest("trolleyCollect", "glass, 10")
        trolleyRequest("trolleyDeposit", "")

        assertNull(getFuncLine(coapRequestTrolley(actor_trolley), "content"))
        val storageContent = getFuncLines(coapRequestTrolley(actor_storage), "content")
        for (cnt in storageContent) {
            val params: List<String> = PrologUtils.extractPayload(cnt)
            when (params[0]) {
                "glass" -> assertEquals(10.0, params[1].toDouble(), 0.0001)
                "plastic" -> assertEquals(0.0, params[1].toDouble(), 0.0001)
            }
        }
    }

    @Test
    fun testTrolleyMove() {
        trolleyRequest("trolleyMove", "3, 4")
        val posLine = getFuncLine(coapRequestTrolley(actor_trolley), "pos")!!
        val posParams = PrologUtils.extractPayload(posLine)
        assertEquals(3, posParams[0].toInt())
        assertEquals(4, posParams[1].toInt())
    }

    private fun trolleyRequest(id: String, params: String) {
        val request: String = MsgUtil.buildRequest(
            "test", id, "$id($params)", actor_trolley
        ).toString()
        var reply: String? = null
        try {
            val connTcp = ConnTcp(SystemConfig.hosts["trolley"], SystemConfig.ports["trolley"]!!)
            ColorsOut.outappl("Asking trolley: $id($params)", ColorsOut.CYAN)
            reply = connTcp.request(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (reply != null && reply.contains("false")) {
            fail("Trolley request <$request> failed!")
        }
    }

    private fun storageDispatch(id: String, params: String) {
        val msg: String = MsgUtil.buildDispatch(
            "test", id, "$id($params)", actor_storage
        ).toString()
        try {
            val connTcp = ConnTcp(SystemConfig.hosts["storage"], SystemConfig.ports["storage"]!!)
            ColorsOut.outappl("Dispatch storage: $id($params)", ColorsOut.CYAN)
            connTcp.forward(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun waitForTrolley() {
        ColorsOut.outappl(this.javaClass.name + " waits for trolley ... ", ColorsOut.GREEN)
        var trolley = getActor(actor_trolley)
        while (trolley == null) {
            CommUtils.delay(200)
            trolley = getActor(actor_trolley)
        }
        ctx_trolley = trolley.context.toString()
        ColorsOut.outappl("Trolley loaded", ColorsOut.GREEN)
    }

    private fun coapRequestTrolley(actor: String): String {
        val reqConn = CoapConnection("${SystemConfig.hosts["storage"]}:${SystemConfig.ports["storage"]}", "$ctx_trolley/$actor")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }
}
