package it.unibo.lenziguerra.wasteservice.statusgui

import it.unibo.kactor.*
import it.unibo.lenziguerra.wasteservice.ContextTestUtils
import it.unibo.lenziguerra.wasteservice.utils.WsConnSpring
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import unibo.comm22.coap.CoapConnection
import unibo.comm22.interfaces.Interaction2021
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import kotlin.concurrent.thread
import kotlin.properties.Delegates


@SpringBootTest
class TestGui {
	companion object {
		const val TROLLEY_ACTOR_NAME = "trolley"
		const val WASTESERVICE_ACTOR_NAME = "wasteservice"
		const val STORAGE_ACTOR_NAME = "storagemanager"
		const val LED_ACTOR_NAME = "led"

		const val TEST_CONTEXT_NAME = "ctx_wasteservice_test"
		const val TEST_CONTEXT_HOST = "localhost"
		const val TEST_CONTEXT_PORT = 8050
		const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "$TEST_CONTEXT_HOST",  "TCP", "$TEST_CONTEXT_PORT")."""
	}

	lateinit var wasteserviceCtx: QakContext
	lateinit var wasteserviceDummyActor: ActorBasic
	lateinit var trolleyDummyActor: ActorBasic
	lateinit var storageDummyActor: ActorBasic
	lateinit var ledDummyActor: ActorBasic

	lateinit var wsConn: Interaction2021

	@LocalServerPort
	private var port: Int = 0

	@BeforeClass
	fun up() {
		CommSystemConfig.tracing = false

		thread { runBlocking {
			val sysRulesResource = javaClass.classLoader.getResource("sysRules.pl")!!
			ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, sysRulesResource.file)
		} }

		waitForContexts()

		replaceWatchedComponent()

		startWsConnection()

		ColorsOut.outappl("Gui tests ready to run!", ColorsOut.GREEN)
	}

	@AfterClass
	fun down() {
		ColorsOut.outappl("Gui tests complete!", ColorsOut.GREEN)
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

	protected fun startWsConnection() {
		wsConn = WsConnSpring(
			java.lang.String.format(
				"ws://%s:%d/truck",
				"localhost",
				port
			)
		)
	}

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
		// Context starts empty, wait for it instead of actors
		var ctx = sysUtil.getContext(TEST_CONTEXT_NAME)
		while (ctx == null) {
			CommUtils.delay(200)
			ctx = sysUtil.getContext(TEST_CONTEXT_NAME)
		}
		wasteserviceCtx = ctx
		ColorsOut.outappl("WasteService loaded", ColorsOut.GREEN)
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
		return wsConn.receiveMsg()
	}

	internal class DummyActor(name: String) : ActorBasic(name) {
		override suspend fun actorBody(msg: IApplMessage) {
		}
	}
}