package it.unibo

import alice.tuprolog.Prolog
import it.unibo.kactor.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import unibo.comm22.coap.CoapConnection
import java.awt.Color
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


class TestGui {
    companion object {
        const val TRIGGER_ACTOR_NAME = "wastetruck"
        const val TROLLEY_ACTOR_NAME = "trolley"
        const val WASTESERVICE_ACTOR_NAME = "wasteservice"
        const val STORAGE_ACTOR_NAME = "storagemanager"
        const val LED_ACTOR_NAME = "blinkled"
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"

        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "localhost",  "TCP", "8050").
            qactor( wasteservicestatusgui, ctx_wasteservice_proto_ctx, "it.unibo.wasteservicestatusgui.Wasteservicestatusgui").
            """

        const val GUI_ACTOR = "wasteservicestatusgui"
        const val GUI_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"
        const val GUI_HOST = "localhost"
        const val GUI_PORT = 8050
    }

    lateinit var wasteserviceCtx: QakContext
    private lateinit var wasteserviceDummyActor: DummyActor
    private lateinit var trolleyDummyActor: DummyActor
    private lateinit var storageDummyActor: DummyActor
    private lateinit var ledDummyActor: DummyActor

    @Before
    fun up() {
        CommSystemConfig.tracing = false

        thread { runBlocking {
            ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, "sysRules.pl")
        } }

        waitForContexts()

        replaceWatchedComponent()
    }

    @After
    fun down() {
        ColorsOut.outappl("Gui test complete!", ColorsOut.GREEN)
    }

    @Test
    fun testGuiPosition() {
        ColorsOut.outappl("Starting gui position test", ColorsOut.CYAN)

        checkGuiResponsePosition("home", "home")
        checkGuiResponsePosition("indoor", "indoor")
        checkGuiResponsePosition("plastic_box", "plastic_box")
    }

    @Test
    fun testGuiState() {
        ColorsOut.outappl("Starting gui trolley state test", ColorsOut.CYAN)

        checkGuiResponseState("work", "work")
        checkGuiResponseState("stopped", "stopped")
    }

    @Test
    fun testGuiLed() {
        ColorsOut.outappl("Starting gui led test", ColorsOut.CYAN)

        checkGuiResponseLed("on", "on")
        checkGuiResponseLed("off", "off")
        checkGuiResponseLed("blinking", "blinking")
    }

    @Test
    fun testGuiStorage() {
        ColorsOut.outappl("Starting gui storage test", ColorsOut.CYAN)

        checkGuiResponseStorage(0, 0)
        checkGuiResponseStorage(15, 15)
    }


    // In model:
    // GUI: Trolley [Position: $TrolleyPos, Status: $TrolleyStatus], Led [$LedStatus], Storage: [Glass: $StorageGlass, Plastic: $StoragePlastic]

    private fun checkGuiResponsePosition(input: String, expectedContent: String) {
        wasteserviceDummyActor.fakeResourceUpdate("tpos($input)")

        CommUtils.delay(500)
        val guiContent = getGuiContent()
        val tposPattern = Regex("(?<=Position:\\s)[^,]+")
        val tposSegment = tposPattern.find(guiContent)?.value ?: throw IllegalStateException("Wrong response $guiContent")
        assertEquals(expectedContent, tposSegment.lowercase())
    }

    private fun checkGuiResponseState(input: String, expectedContent: String) {
        wasteserviceDummyActor.fakeResourceUpdate("tpos(indoor)")
        trolleyDummyActor.fakeResourceUpdate("state($input)\npos(-1,-1)")

        CommUtils.delay(500)
        val guiContent = getGuiContent()
        val tstatePattern = Regex("(?<=Status:\\s)[^]]+")
        val tstateSegment = tstatePattern.find(guiContent)!!.value
        assertEquals(expectedContent, tstateSegment.lowercase())
    }

    private fun checkGuiResponseLed(input: String, expectedContent: String) {
        ledDummyActor.fakeResourceUpdate("ledState($input)")

        CommUtils.delay(500)
        val guiContent = getGuiContent()
        val ledPattern = Regex("(?<=Led\\s\\[)[^]]+")
        val ledSegment = ledPattern.find(guiContent)!!.value
        assertEquals(expectedContent, ledSegment.lowercase())
    }

    private fun checkGuiResponseStorage(input: Int, expectedContent: Int) {
        // Checks against glass, for simplicity
        storageDummyActor.fakeResourceUpdate("content(glass,$input)\ncontent(plastic,0)")

        CommUtils.delay(500)
        val guiContent = getGuiContent()
        val glassPattern = Regex("(?<=Glass:\\s)[^,]+")
        val glassSegment = glassPattern.find(guiContent)!!.value
        assertEquals(expectedContent, glassSegment.toFloat().toInt())
    }

    private fun waitForContexts() {
        ColorsOut.outappl(this.javaClass.name + " waits for WasteService ... ", ColorsOut.GREEN)
        waitForActor(GUI_ACTOR)
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

    private fun replaceWatchedComponent() {
        // Rimuovi attori emettitori/osservati, crea falsi attori
        // o componenti software controllati da noi
        trolleyDummyActor = DummyActor(TROLLEY_ACTOR_NAME)
        wasteserviceDummyActor = DummyActor(WASTESERVICE_ACTOR_NAME)
        storageDummyActor = DummyActor(STORAGE_ACTOR_NAME)
        ledDummyActor = DummyActor(LED_ACTOR_NAME)

        wasteserviceCtx.addActor(trolleyDummyActor)
        wasteserviceCtx.addActor(wasteserviceDummyActor)
        wasteserviceCtx.addActor(storageDummyActor)
        wasteserviceCtx.addActor(ledDummyActor)
        ColorsOut.outappl("WasteService: added fake actors <${trolleyDummyActor.name}> <${wasteserviceDummyActor.name}> <${storageDummyActor.name}> <${ledDummyActor.name}>", ColorsOut.GREEN)
    }

    private fun coapRequest(actor: String): String? {
        val reqConn = CoapConnection("$GUI_HOST:$GUI_PORT", "$GUI_CONTEXT_NAME/$actor")
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    private fun getGuiContent(): String {
        return coapRequest(GUI_ACTOR)!!
    }

    internal class DummyActor(name: String) : ActorBasic(name) {
        override suspend fun actorBody(msg: IApplMessage) {
        }

        fun fakeResourceUpdate(data: String) {
            updateResourceRep(data)
            ColorsOut.outappl("DummyActor | ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())} Faked $name update: $data", ColorsOut.ANSI_PURPLE)
        }
    }
}