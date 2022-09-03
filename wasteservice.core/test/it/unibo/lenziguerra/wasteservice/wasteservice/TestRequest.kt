package it.unibo.lenziguerra.wasteservice.wasteservice

import it.unibo.kactor.*
import it.unibo.lenziguerra.wasteservice.*
import it.unibo.lenziguerra.wasteservice.data.StorageStatus
import it.unibo.lenziguerra.wasteservice.utils.LogUtils
import it.unibo.lenziguerra.wasteservice.utils.MsgUtilsWs.cleanMessage
import it.unibo.lenziguerra.wasteservice.utils.WsConnSpring
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import unibo.comm22.coap.CoapConnection
import unibo.comm22.interfaces.Interaction2021
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommUtils
import kotlin.concurrent.thread

@SpringBootTest(classes = [WasteserviceApplication::class], webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("noqak")
@RunWith(SpringRunner::class)
class TestRequest {
    companion object {
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_test_request"
        const val TEST_CONTEXT_HOST = "localhost"
        const val TEST_CONTEXT_PORT = 9650
        private const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "$TEST_CONTEXT_HOST",  "TCP", "$TEST_CONTEXT_PORT").
            qactor( storagemanager, $TEST_CONTEXT_NAME, "it.unibo.storagemanager.Storagemanager").
            qactor( wasteservice, $TEST_CONTEXT_NAME, "it.unibo.wasteservice.Wasteservice").
            qactor( trolley, $TEST_CONTEXT_NAME, "it.unibo.trolley.Trolley").
        """

        private var qakContext: QakContext? = null

        @BeforeAll
        @JvmStatic
        fun upClass() {
            SystemConfig.positions["home"] = listOf(listOf(0, 0), listOf(0, 0))
            SystemConfig.positions["indoor"] = listOf(listOf(0, 1), listOf(1, 1))
            SystemConfig.positions["plastic_box"] = listOf(listOf(2, 1), listOf(2, 1))
            SystemConfig.positions["glass_box"] = listOf(listOf(3, 1), listOf(3, 1))

            SystemConfig.hosts.replaceAll { _, _ -> "localhost" }
            SystemConfig.contexts.forEach {
                SystemConfig.ports[it.key] = TEST_CONTEXT_PORT
            }
            SystemConfig.contexts.replaceAll { _, _ -> TEST_CONTEXT_NAME }

            SystemConfig.disableRead()

            thread { runBlocking {
                ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, "sysRules.pl", TEST_CONTEXT_NAME)
            } }

            waitForActors()
            addTestActors()

            LogUtils.threadOut("TestRequest ready!", ColorsOut.GREEN)
        }

        @AfterAll
        @JvmStatic
        fun downClass() {
            qakContext?.terminateTheContext()
        }

        private fun waitForActors() {
            val actorsToWait = listOf("storagemanager", "wasteservice", "trolley")

            LogUtils.threadOut(this::class.java.name + " waits for actors ... ", ColorsOut.GREEN)

            actorsToWait.forEach {
                var actor = QakContext.getActor(it)
                while (actor == null) {
                    CommUtils.delay(200)
                    actor = QakContext.getActor(it)
                }
            }

            while (qakContext == null) {
                CommUtils.delay(200)
                qakContext = sysUtil.getContext(TEST_CONTEXT_NAME)
            }

            LogUtils.threadOut("Actors loaded", ColorsOut.GREEN)
        }

        private fun addTestActors() {
            val pathexec = PathExecDummyImmediate("pathexecstop")
            qakContext?.addActor(pathexec)

            LogUtils.threadOut("Added test actors <${pathexec.name}>", ColorsOut.GREEN)
        }
    }

    @Autowired
    private lateinit var controller: WasteServiceController
    @LocalServerPort
    private val port: Int? = null

    private lateinit var wsConn: Interaction2021

    @BeforeEach
    fun up() {
        startWsConnection()
        LogUtils.threadOut("Starting TestRequest, port is $port", ColorsOut.CYAN)
        dispatch(
            "testStorageReset",
            "",
            SystemConfig.actors["storage"]!!
        )
    }

    @AfterEach
    fun down() {
        LogUtils.threadOut(this.javaClass.name + " TEST END", ColorsOut.GREEN)
    }

    @Test
    fun testAccept() {
        println("Start testAccept")
        val reply: String = askDeposit("glass", 1)
        LogUtils.threadOut("Reply: $reply", ColorsOut.CYAN)
        assertTrue(reply.contains("loadaccept"))
    }


    @Test
    fun testDeny() {
        println("Start testDeny")
        val amount = 10
        val type = "plastic"
        val storageStatus = StorageStatus.fromProlog(coapRequest("storage"))
        val amountTooMuch = storageStatus.maxAmounts[WasteType.valueOf(type.uppercase())]!! - amount + 5

        dispatch(
            "testStorageSet",
            "testStorageSet('\"$type\":$amountTooMuch')",
            SystemConfig.actors["storage"]!!
        )

        val reply: String = askDeposit(type, amount)
        LogUtils.threadOut("Reply: $reply", ColorsOut.CYAN)
        assertTrue(reply.contains("loadrejected"))
    }

    @Test
    fun testPickedUp() {
        println("Start testPickedUp")
        dispatch(
            "testStorageReset",
            "",
            SystemConfig.actors["storage"]!!
        )
        val reply = askDeposit("glass", 1)
        LogUtils.threadOut("Reply: $reply", ColorsOut.CYAN)
        val dispatch = wsConn.receiveMsg()
        LogUtils.threadOut("Received: $dispatch", ColorsOut.CYAN)
        assertTrue(dispatch.contains("pickedUp"))
    }

    protected fun askDeposit(type: String, amount: Int): String {
        return try {
            wsConn.request("loadDeposit($type, $amount)")
        } catch (e: Exception) {
            e.printStackTrace()
            fail(e.message)
        }
    }

    protected fun startWsConnection() {
        wsConn = WsConnSpring("ws://localhost:$port/truck")
    }

    protected fun coapRequest(element: String): String {
        val reqConn = CoapConnection(
            "$TEST_CONTEXT_HOST:$TEST_CONTEXT_PORT",
            "$TEST_CONTEXT_NAME/${SystemConfig.actors[element]!!}"
        )
        val answer = reqConn.request("")
        LogUtils.threadOut("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    protected fun dispatch(msgId: String, content: String, dest: String, actor: String = "test") {
        try {
            val msg = MsgUtil.buildDispatch(actor, msgId, content, dest)
            LogUtils.threadOut("Sending " + cleanMessage(msg), ColorsOut.CYAN)
            runBlocking {
                MsgUtil.sendMsg(msg, QakContext.getActor(msg.msgReceiver())!!)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail(e.message)
        }
    }
}