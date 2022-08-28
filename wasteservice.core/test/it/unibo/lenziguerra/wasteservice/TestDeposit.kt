package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.MsgUtil.buildDispatch
import it.unibo.kactor.MsgUtil.buildRequest
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import it.unibo.lenziguerra.wasteservice.SystemConfig.contexts
import it.unibo.lenziguerra.wasteservice.data.StorageStatus
import it.unibo.lenziguerra.wasteservice.utils.LogUtils
import it.unibo.lenziguerra.wasteservice.utils.MsgUtilsWs
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils.extractPayload
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils.getFuncLines
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.eclipse.californium.core.CoapObserveRelation
import org.junit.Assert
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import unibo.comm22.coap.CoapConnection
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommUtils
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class TestDeposit {
    companion object {
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_test"
        const val TEST_CONTEXT_HOST = "localhost"
        const val TEST_CONTEXT_PORT = 9651
        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "$TEST_CONTEXT_HOST",  "TCP", "$TEST_CONTEXT_PORT").
            qactor( storagemanager, $TEST_CONTEXT_NAME, "it.unibo.storagemanager.Storagemanager").
            qactor( wasteservice, $TEST_CONTEXT_NAME, "it.unibo.wasteservice.Wasteservice").
            qactor( trolley, $TEST_CONTEXT_NAME, "it.unibo.trolley.Trolley").
        """

        lateinit var qakContext: QakContext

        lateinit var trolleyCoapConnection: CoapConnection
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
                ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, "sysRules.pl")
            } }

            waitForActors()
            addTestActors()
            startCoapConnections()

            LogUtils.threadOut("TestDeposit ready!", ColorsOut.GREEN)
        }

        @AfterAll
        @JvmStatic
        fun downClass() {
            qakContext.terminateTheContext()
        }

        fun waitForActors() {
            val actorsToWait = listOf("storagemanager", "wasteservice", "trolley")

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
            val pathexec = PathExecDummyImmediate("pathexecstop")
            qakContext.addActor(pathexec)

            LogUtils.threadOut("Added test actors <${pathexec.name} as ${pathexec::class}>", ColorsOut.GREEN)
        }

        fun startCoapConnections() {
            trolleyCoapConnection = CoapConnection(
                "$TEST_CONTEXT_HOST:$TEST_CONTEXT_PORT",
                "$TEST_CONTEXT_NAME/trolley"
            )
            LogUtils.threadOut("connected via Coap conn: $trolleyCoapConnection", ColorsOut.CYAN)
            wasteserviceCoapConnection = CoapConnection(
                "$TEST_CONTEXT_HOST:$TEST_CONTEXT_PORT",
                "$TEST_CONTEXT_NAME/wasteservice"
            )
            LogUtils.threadOut("connected via Coap conn: $wasteserviceCoapConnection", ColorsOut.CYAN)
        }
    }

    lateinit var trolleyPosObserver: TrolleyPosObserver
    lateinit var wasteServiceObserver: WasteServiceTrolleyPosObserver
    val coapObservers = mutableMapOf<CoapConnection, CoapObserveRelation>()

    @BeforeEach
    fun up() {
        startObservingTrolley()
        startObservingWasteservice()
        waitForHome()
    }

    @AfterEach
    fun down() {
        cleanupObservers()
        LogUtils.threadOut(this.javaClass.name + " TEST END", ColorsOut.GREEN)
    }

    @Test
    fun testPositionsGlass() {
        startDeposit("glass", 10)
        val expectedPositions = listOf("home", "indoor", "glass_box")
        val expectedPositionsCoords = listOf(
            SystemConfig.positions["home"]!!,
            SystemConfig.positions["indoor"]!!,
            SystemConfig.positions["glass_box"]!!,
        )
        val maxSecondsWait = 10
        positionsTest(expectedPositions, expectedPositionsCoords, maxSecondsWait)
    }

    @Test
    fun testPositionsPlastic() {
        startDeposit("plastic", 10)
        val expectedPositions = listOf("home", "indoor", "plastic_box")
        val expectedPositionsCoords = listOf(
            SystemConfig.positions["home"]!!,
            SystemConfig.positions["indoor"]!!,
            SystemConfig.positions["plastic_box"]!!,
        )
        val maxSecondsWait = 10
        positionsTest(expectedPositions, expectedPositionsCoords, maxSecondsWait)
    }

    @Test
    fun testDeposit() {
        dispatch(
            "testStorageReset",
            "testStorageReset()",
            SystemConfig.actors["storage"]!!
        )
        startDeposit("glass", 15)
        val maxSecondsWait = 10
        for (i in 0 until maxSecondsWait) {
            CommUtils.delay(1000)
            val lastPos = wasteServiceObserver.history.last()
            if (lastPos == "error") {
                fail("Movement error! Pos history is " + wasteServiceObserver.history)
            } else if (lastPos == "glass_box") {
                // Lascia tempo di scaricare pesi nel caso la posizione
                // sia stata appena raggiunta
                CommUtils.delay(1000)
                val storageStatus = StorageStatus.fromProlog(coapRequest("storage"))
                assertEquals(15.0f, storageStatus.amounts[WasteType.GLASS]!!, 0.001f)
                assertEquals(0f, storageStatus.amounts[WasteType.PLASTIC]!!, 0.001f)
                return
            }
        }
        fail("Too much time to reach final position, pos history was: ${wasteServiceObserver.history}")
    }

    private fun positionsTest(expectedPositions: List<String>, expectedCoords: List<List<List<Int>>>, maxSecondsWait: Int) {
        resetObserverSemaphores()
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < maxSecondsWait * 1000) {
            waitForObserverUpdate(maxSecondsWait * 1000)
            val posHistory = wasteServiceObserver.history
            val coordHistory = trolleyPosObserver.history
            LogUtils.threadOut("positionsTest: start iteration, known histories: $posHistory, ${coordHistory.map { it.contentToString() }}", ColorsOut.CYAN)

            if (posHistory.last() == "error") {
                fail("Movement error! Pos history is " + posHistory);
            }
            for (j in expectedPositions.indices) {
                if (j >= posHistory.size) {
                    break
                } else {
                    assertEquals(expectedPositions[j], posHistory[j]);
                }
            }

            // Se ogni posizione corrisponde, e si è raggiunta l'ultima, ritorna con successo
            // se corrispondono anche le coordinate
            if (expectedPositions.size == posHistory.size) {
                // Controlla che passi dalle coordinate richieste (non SOLO le coordinate richieste)
                var matchedCoords = 0
                for (coord in coordHistory) {
                    if (matchesOneCoordInRectangle(coord, expectedCoords[matchedCoords])) {
                        matchedCoords++;
                    }
                }
                if (matchedCoords == expectedCoords.size) {
                    return;
                } else {
                    fail("Finished positions but coordinates didn't match: ${coordHistory.map { it.contentToString() }}")
                }
            }
        }
    }

    private fun resetObserverSemaphores() {
        wasteServiceObserver.semaphore.drainPermits()
        trolleyPosObserver.semaphore.drainPermits()
        LogUtils.threadOut("Reset semaphores for observers")
    }

    private fun waitForObserverUpdate(maxTimeMillis: Int) {
        try {
            if (!wasteServiceObserver.semaphore.tryAcquire(1, maxTimeMillis.toLong(), TimeUnit.MILLISECONDS)) {
                LogUtils.threadOut("Position check timeout", ColorsOut.BLUE)
            }
            if (!trolleyPosObserver.semaphore.tryAcquire(1, maxTimeMillis.toLong(), TimeUnit.MILLISECONDS)) {
                LogUtils.threadOut("Position check timeout", ColorsOut.BLUE)
            }
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    fun matchesOneCoordInRectangle(coord: IntArray, checkCorners: List<List<Int>>): Boolean {
        for (i in checkCorners[0][0]..checkCorners[1][0]) {
            for (j in checkCorners[0][1]..checkCorners[1][1]) {
                if (coord[0] == i && coord[1] == j) return true
            }
        }
        return false
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
            Assertions.fail(e.message)
        }
    }

    fun startObservingTrolley() {
        trolleyPosObserver = TrolleyPosObserver()
        coapObservers[trolleyCoapConnection] = trolleyCoapConnection.observeResource(trolleyPosObserver)
        LogUtils.threadOut("Added trolley observer", ColorsOut.CYAN)

        // Force initial update
        CommUtils.delay(200)
        QakContext.getActor("wasteservice")!!.changed()
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
        trolleyPosObserver.testend = true
        wasteServiceObserver.testend = true
        println("Cleaned up observers")
    }

    fun waitForHome() {
        println("Waiting for components to be at home")
        while(true) {
            waitForObserverUpdate(5000)
            if (
                wasteServiceObserver.history.last() == "home" &&
                trolleyPosObserver.history.last().contentEquals(intArrayOf(0,0))
            ) {
                break
            }
        }
        wasteServiceObserver.history.let { it.subList(0, it.size-1).clear() }
        trolleyPosObserver.history.let { it.subList(0, it.size-1).clear() }
        println("Observers detected ws&tr at home, can start")
    }
}