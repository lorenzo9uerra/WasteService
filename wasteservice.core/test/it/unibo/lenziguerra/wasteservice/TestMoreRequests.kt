package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.MsgUtil.buildRequest
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import it.unibo.lenziguerra.wasteservice.utils.LogUtils
import it.unibo.lenziguerra.wasteservice.utils.MsgUtilsWs
import kotlinx.coroutines.runBlocking
import org.eclipse.californium.core.CoapObserveRelation
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import unibo.comm22.coap.CoapConnection
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommUtils
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class TestMoreRequests {
    companion object {
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_test_more_requests"
        const val TEST_CONTEXT_HOST = "localhost"
        const val TEST_CONTEXT_PORT = 9653
        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "$TEST_CONTEXT_HOST",  "TCP", "$TEST_CONTEXT_PORT").
            qactor( storagemanager, $TEST_CONTEXT_NAME, "it.unibo.storagemanager.Storagemanager").
            qactor( wasteservice, $TEST_CONTEXT_NAME, "it.unibo.wasteservice.Wasteservice").
            qactor( trolley, $TEST_CONTEXT_NAME, "it.unibo.trolley.Trolley").
        """

        private var qakContext: QakContext? = null

        lateinit var wasteserviceCoapConnection: CoapConnection

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
            startCoapConnections()

            LogUtils.threadOut("TestMoreRequests ready!", ColorsOut.GREEN)
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

            while (qakContext == null){
                CommUtils.delay(200)
                qakContext = sysUtil.getContext(TEST_CONTEXT_NAME)
            }


            LogUtils.threadOut("Actors loaded", ColorsOut.GREEN)
        }

        private fun addTestActors() {
            val pathexec = PathExecDummyImmediate("pathexecstop")
            qakContext?.addActor(pathexec)

            LogUtils.threadOut("Added test actors <${pathexec.name} as ${pathexec::class}>", ColorsOut.GREEN)
        }

        fun startCoapConnections() {
            wasteserviceCoapConnection = CoapConnection(
                "$TEST_CONTEXT_HOST:$TEST_CONTEXT_PORT",
                "$TEST_CONTEXT_NAME/wasteservice"
            )
            LogUtils.threadOut("connected via Coap conn: $wasteserviceCoapConnection", ColorsOut.CYAN)
        }
    }

    lateinit var wasteServiceObserver: WasteServiceTrolleyPosObserver
    val coapObservers = mutableMapOf<CoapConnection, CoapObserveRelation>()

    @BeforeEach
    fun up() {
        LogUtils.threadOut(this.javaClass.name + " TEST START", ColorsOut.GREEN)
        startObservingWasteservice()
        waitForHome()
    }

    @AfterEach
    fun down() {
        cleanupObservers()
        LogUtils.threadOut(this.javaClass.name + " TEST END", ColorsOut.GREEN)
    }

    @Test
    fun testGoHome() {
        startDeposit("glass", 10)
        val expectedPositions = listOf("home", "indoor", "glass_box", "home")
        simplePositionsTest(expectedPositions, maxSecondsWait = 10)
    }

    @Test
    fun testGoIndoor() {
        startDeposit("glass", 10)
        CommUtils.delay(500)
        startDeposit("glass", 10)
        val expectedPositions = listOf("home", "indoor", "glass_box", "indoor")
        simplePositionsTest(expectedPositions, maxSecondsWait = 10)
    }

    fun simplePositionsTest(expectedPositions: List<String>, maxSecondsWait: Int) {
        resetObserverSemaphores()
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < maxSecondsWait * 1000) {
            waitForObserverUpdate(1000 * maxSecondsWait)
            ColorsOut.outappl("Checking position...", ColorsOut.BLUE)
            val posHistory = wasteServiceObserver.history
            if (posHistory.last() == "error") {
                fail("Movement error! Pos history is $posHistory")
            }
            for (j in expectedPositions.indices) {
                if (j >= posHistory.size) {
                    break
                } else {
                    assertEquals(expectedPositions[j], posHistory[j])
                }
            }

            // Se ogni posizione corrisponde, e si è raggiunta l'ultima, ritorna con successo
            if (expectedPositions.size <= posHistory.size) {
                return
            }
        }
        fail("Too much time to reach final position, pos history was: ${wasteServiceObserver.history}")
    }

    private fun resetObserverSemaphores() {
        wasteServiceObserver.semaphore.drainPermits()
        LogUtils.threadOut("Reset semaphores for observers")
    }

    private fun waitForObserverUpdate(maxTimeMillis: Int) {
        try {
            if (!wasteServiceObserver.semaphore.tryAcquire(1, maxTimeMillis.toLong(), TimeUnit.MILLISECONDS)) {
                LogUtils.threadOut("Position check timeout", ColorsOut.BLUE)
            }
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    fun startDeposit(type: String, amount: Int) {
        val startDepositDispatch = buildRequest(
            "test", "triggerDeposit",
            "triggerDeposit($type, $amount)",
            "wasteservice"
        ).toString()
        try {
            val connTcp = ConnTcp(TEST_CONTEXT_HOST, TEST_CONTEXT_PORT)
            connTcp.request(startDepositDispatch)
            LogUtils.threadOut("STARTED DEPOSIT VIA DISPATCH", ColorsOut.GREEN)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun coapRequest(element: String): String {
        val reqConn = CoapConnection(
            "${TEST_CONTEXT_HOST}:${TEST_CONTEXT_PORT}",
            "${TEST_CONTEXT_NAME}/${SystemConfig.actors[element]!!}"
        )
        val answer = reqConn.request("")
        LogUtils.threadOut("coapRequest answer=$answer", ColorsOut.CYAN)
        return if (answer != "0") {
            answer
        } else {
            throw IOException("Response null! Wrong element? Called with $element to ip " +
                    "${TEST_CONTEXT_HOST}:${TEST_CONTEXT_PORT}" +
                    "/${TEST_CONTEXT_NAME}/${SystemConfig.actors[element]!!}")
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

    fun startObservingWasteservice() {
        wasteServiceObserver = WasteServiceTrolleyPosObserver()
        coapObservers[wasteserviceCoapConnection] = wasteserviceCoapConnection.observeResource(wasteServiceObserver)
        LogUtils.threadOut("Added ws observer", ColorsOut.CYAN)

        // Force initial update
        CommUtils.delay(200)
        QakContext.getActor("trolley")!!.changed()
    }

    fun cleanupObservers() {
        coapObservers.forEach { it.key.removeObserve(it.value) }
        coapObservers.clear()
        wasteServiceObserver.testend = true
        println("Cleaned up observers")
    }

    fun waitForHome() {
        println("Waiting for components to be at home")
        while(true) {
            waitForObserverUpdate(5000)
            if (wasteServiceObserver.history.last() == "home") {
                break
            }
        }
        wasteServiceObserver.history.let { it.subList(0, it.size-1).clear() }
        println("Observers detected ws&tr at home, can start")
    }
}