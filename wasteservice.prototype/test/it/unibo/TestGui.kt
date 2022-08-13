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
import kotlin.concurrent.thread


class TestGui {
    companion object {
        const val TRIGGER_ACTOR_NAME = "wastetruck"
        const val TROLLEY_ACTOR_NAME = "trolley"
        const val WASTESERVICE_ACTOR_NAME = "wasteservice"
        const val STORAGE_ACTOR_NAME = "storagemanager"
        const val LED_ACTOR_NAME = "led"
        const val TEST_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"

        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "localhost",  "TCP", "8050").
            qactor( gui, ctx_wasteservice_proto_ctx, "it.unibo.gui.Gui").
            """

        const val GUI_ACTOR = "gui"
        const val GUI_CONTEXT_NAME = "ctx_wasteservice_proto_ctx"
        const val GUI_HOST = "localhost"
        const val GUI_PORT = 8050
    }

    lateinit var wasteserviceCtx: QakContext
    lateinit var wasteserviceDummyActor: ActorBasic
    lateinit var trolleyDummyActor: ActorBasic
    lateinit var storageDummyActor: ActorBasic
    lateinit var ledDummyActor: ActorBasic

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
        wasteserviceDummyActor.updateResourceRep("tpos($input)")
        ColorsOut.outappl("Sent wasteservice resource update tpos($input)", ColorsOut.ANSI_PURPLE)

        CommUtils.delay(500)
        val guiContent = getGuiContent()
        val tposPattern = Regex("(?<=Position:\\s)[^,]+")
        val tposSegment = tposPattern.find(guiContent)?.value ?: throw IllegalStateException("Wrong response $guiContent")
        assertEquals(expectedContent, tposSegment.lowercase())
    }

    private fun checkGuiResponseState(input: String, expectedContent: String) {
        wasteserviceDummyActor.updateResourceRep("tpos(indoor)")
        trolleyDummyActor.updateResourceRep("state($input)\npos(-1,-1)")
        ColorsOut.outappl("Sent trolley resource update state($input)\\npos(-1,-1)", ColorsOut.ANSI_PURPLE)

        CommUtils.delay(500)
        val guiContent = getGuiContent()
        val tstatePattern = Regex("(?<=Status:\\s)[^]]+")
        val tstateSegment = tstatePattern.find(guiContent)!!.value
        assertEquals(expectedContent, tstateSegment.lowercase())
    }

    private fun checkGuiResponseLed(input: String, expectedContent: String) {
        ledDummyActor.updateResourceRep("ledState($input)")
        ColorsOut.outappl("Sent led resource update ledState($input)", ColorsOut.ANSI_PURPLE)

        CommUtils.delay(500)
        val guiContent = getGuiContent()
        val ledPattern = Regex("(?<=Led\\s\\[)[^]]+")
        val ledSegment = ledPattern.find(guiContent)!!.value
        assertEquals(expectedContent, ledSegment.lowercase())
    }

    private fun checkGuiResponseStorage(input: Int, expectedContent: Int) {
        // Checks against glass, for simplicity
        storageDummyActor.updateResourceRep("content(glass,$input)\ncontent(plastic,0)")
        ColorsOut.outappl("Sent storage resource update content(glass,$input)\\ncontent(plastic,0)", ColorsOut.ANSI_PURPLE)

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
    }
}