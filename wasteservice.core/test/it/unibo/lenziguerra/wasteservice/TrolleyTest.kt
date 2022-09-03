package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import it.unibo.lenziguerra.wasteservice.data.StorageStatus
import it.unibo.lenziguerra.wasteservice.data.TrolleyStatus
import it.unibo.lenziguerra.wasteservice.utils.LogUtils
import it.unibo.lenziguerra.wasteservice.utils.MsgUtilsWs
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import unibo.comm22.coap.CoapConnection
import unibo.comm22.tcp.TcpClientSupport
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommUtils
import java.io.IOException
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread


class TrolleyTest() {
    companion object {
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_test"
        const val TEST_CONTEXT_HOST = "localhost"
        const val TEST_CONTEXT_PORT = 9652
        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "$TEST_CONTEXT_HOST",  "TCP", "$TEST_CONTEXT_PORT").
            qactor( storagemanager, $TEST_CONTEXT_NAME, "it.unibo.storagemanager.Storagemanager").
            qactor( trolley, $TEST_CONTEXT_NAME, "it.unibo.trolley.Trolley").
        """

        lateinit var qakContext: QakContext

        lateinit var pathexec: PathExecDummyVerifyPath

        @BeforeAll
        @JvmStatic
        fun upClass() {
            SystemConfig.hosts.replaceAll { _, _ -> "localhost" }
            SystemConfig.contexts.forEach {
                SystemConfig.ports[it.key] = TEST_CONTEXT_PORT
            }
            SystemConfig.contexts.replaceAll { _, _ -> TEST_CONTEXT_NAME }

            SystemConfig.disableRead()

            thread { runBlocking {
                ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, "sysRules.pl")
            } }

            waitForActors()
            addTestActors()

            LogUtils.threadOut("TrolleyTest ready!", ColorsOut.GREEN)
        }

        @AfterAll
        @JvmStatic
        fun downClass() {
            qakContext.terminateTheContext()
        }

        fun waitForActors() {
            val actorsToWait = listOf("storagemanager", "trolley")

            LogUtils.threadOut(this::class.java.name + " waits for actors ... ", ColorsOut.GREEN)

            actorsToWait.forEach {
                var actor = QakContext.getActor(it)
                while (actor == null) {
                    CommUtils.delay(200)
                    actor = QakContext.getActor(it)
                }
            }

            qakContext = sysUtil.getContext(TEST_CONTEXT_NAME)!!

            LogUtils.threadOut("Actors loaded", ColorsOut.GREEN)
        }

        fun addTestActors() {
            pathexec = PathExecDummyVerifyPath("pathexecstop")
            qakContext.addActor(pathexec)

            LogUtils.threadOut("Added test actors <${pathexec.name} as ${pathexec::class}>", ColorsOut.GREEN)
        }
    }

    lateinit var ctx_trolley: String

    @BeforeEach
    fun up() {
        LogUtils.threadOut(this.javaClass.name + " TEST START", ColorsOut.GREEN)
    }

    @AfterEach
    fun down() {
        LogUtils.threadOut(this.javaClass.name + " TEST END", ColorsOut.GREEN)
    }

    @Test
    fun testTrolleyCollect() {
        val statusBefore = TrolleyStatus.fromProlog(coapRequest("trolley"))

        trolleyRequest("trolleyCollect", "glass, 10")
        waitForIdle()
        val statusAfter = TrolleyStatus.fromProlog(coapRequest("trolley"))

        assertEquals(WasteType.GLASS, statusAfter.contentType)
        assertEquals(10f, statusAfter.contentAmount - statusBefore.contentAmount, 0.0001f)
    }

    @Test
    fun testTrolleyDeposit() {
        dispatch("testStorageReset", "", "storagemanager")

        trolleyRequest("trolleyCollect", "glass, 10")
        waitForIdle()
        trolleyRequest("trolleyDeposit", "")
        waitForIdle()

        assertNull(TrolleyStatus.fromProlog(coapRequest("trolley")).contentType, "content")
        val storageStatus = StorageStatus.fromProlog(coapRequest("storage"))
        assertEquals(10f, storageStatus.amounts[WasteType.GLASS]!!, 0.0001f)
        assertEquals(0f, storageStatus.amounts[WasteType.PLASTIC]!!, 0.0001f)
    }

    @Test
    fun testTrolleyMove() {
        trolleyRequest("trolleyMove", "3, 4")
        val pos = TrolleyStatus.fromProlog(coapRequest("trolley")).pos
        assertEquals(3, pos[0])
        assertEquals(4, pos[1])
        // Controllo con posizione "reale"
        val pathexecPos = pathexec.robotPosition
        assertTrue(pos.contentEquals(pathexecPos), "Trolley Position not equal to Real Position")
    }

    fun waitForIdle() {
        LogUtils.threadOut("Waiting for trolley to finish activity...")
        var trolleyStatus = TrolleyStatus.fromProlog(coapRequest("trolley"))
        while (trolleyStatus.activity != TrolleyStatus.Activity.IDLE) {
            CommUtils.delay(200)
            trolleyStatus = TrolleyStatus.fromProlog(coapRequest("trolley"))
        }
    }

    fun trolleyRequest(msgId: String, params: String, actor: String = "test") {
        val msg = MsgUtil.buildRequest(actor, msgId, "$msgId(${if (params == "") "_" else params})", "trolley")
        val conn = TcpClientSupport.connect(TEST_CONTEXT_HOST, TEST_CONTEXT_PORT, 5)
        LogUtils.threadOut("Sending " + MsgUtilsWs.cleanMessage(msg), ColorsOut.CYAN)
        val replyFuture = CompletableFuture<String>()
        thread {
            val reply = conn.request(msg.toString())
            replyFuture.complete(reply)
        }
        CommUtils.delay(5000)
        if (!replyFuture.isDone) {
            fail<Unit>("Trolley request <$msg> timeout!")
        }
        if (replyFuture.get().contains("trolleyFail")) {
            fail<Unit>("Trolley request <$msg> failed!")
        }
    }

    fun dispatch(msgId: String, content: String, dest: String, actor: String = "test") {
        try {
            val msg = MsgUtil.buildDispatch(actor, msgId, content, dest)
            LogUtils.threadOut("Sending " + MsgUtilsWs.cleanMessage(msg), ColorsOut.CYAN)
            runBlocking {
                MsgUtil.sendMsg(msg, QakContext.getActor(msg.msgReceiver())!!)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail(e.message)
        }
    }

    private fun coapRequest(element: String): String {
        val reqConn = CoapConnection(
            "${TEST_CONTEXT_HOST}:${TEST_CONTEXT_PORT}",
            "${TEST_CONTEXT_NAME}/${SystemConfig.actors[element]!!}"
        )
        var answer = reqConn.request("")
        val time = System.currentTimeMillis()
        while (answer == "" && System.currentTimeMillis() - time < 10000) {
            answer = reqConn.request("")
            CommUtils.delay(200)
        }
        LogUtils.threadOut("coapRequest answer=$answer", ColorsOut.CYAN)
        return if (answer != "0") {
            answer
        } else {
            throw IOException("Response null! Wrong element? Called with $element to ip " +
                    "${TEST_CONTEXT_HOST}:${TEST_CONTEXT_PORT}" +
                    "/${TEST_CONTEXT_NAME}/${SystemConfig.actors[element]!!}")
        }
    }
}
