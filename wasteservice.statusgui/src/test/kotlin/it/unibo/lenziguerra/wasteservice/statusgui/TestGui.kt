package it.unibo.lenziguerra.wasteservice.statusgui

import it.unibo.kactor.*
import it.unibo.lenziguerra.wasteservice.ContextTestUtils
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.lenziguerra.wasteservice.data.TrolleyStatus
import it.unibo.lenziguerra.wasteservice.utils.WsConnSpring
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.*
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.server.resources.CoapExchange
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner
import unibo.comm22.coap.CoapConnection
import unibo.comm22.interfaces.Interaction2021
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import kotlin.concurrent.thread
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.format.DateTimeFormatter

@SpringBootTest(classes = [WasteServiceStatusGUIApplication::class], webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RunWith(SpringRunner::class)
class TestGui {
	companion object {
		const val TROLLEY_ACTOR_NAME = "trolley"
		const val WASTESERVICE_ACTOR_NAME = "wasteservice"
		const val STORAGE_ACTOR_NAME = "storagemanager"

		const val TEST_CONTEXT_NAME = "ctx_wasteservice_test"
		const val TEST_CONTEXT_HOST = "localhost"
		const val TEST_CONTEXT_PORT = 9654
		const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "$TEST_CONTEXT_HOST",  "TCP", "$TEST_CONTEXT_PORT")."""

		const val FAKE_LED_PORT = 9655
		const val FAKE_LED_HOST = "localhost"

		lateinit var wasteserviceCtx: QakContext

		private lateinit var wasteserviceDummyActor: DummyActor
		private lateinit var trolleyDummyActor: DummyActor
		private lateinit var storageDummyActor: DummyActor
		private lateinit var ledCoapResource: DummyCoapResource

		@BeforeAll
		@JvmStatic
		fun beforeAll() {
			thread { runBlocking {
				val sysRulesResource = javaClass.classLoader.getResource("sysRules.pl")!!
				ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, sysRulesResource.file)
			} }

			waitForContexts()

			replaceWatchedComponent()

			SystemConfig.disableRead()
			SystemConfig.disableWrite()

			for (id in arrayOf("trolley", "wasteServiceContext", "storage")) {
				SystemConfig.hosts[id] = TEST_CONTEXT_HOST
				SystemConfig.ports[id] = TEST_CONTEXT_PORT
				SystemConfig.contexts[id] = TEST_CONTEXT_NAME
			}

			SystemConfig.hosts["led"] = FAKE_LED_HOST
			SystemConfig.ports["led"] = FAKE_LED_PORT

			ColorsOut.outappl("Gui tests ready!", ColorsOut.GREEN)
		}

		@AfterAll
		@JvmStatic
		fun afterAll() {
			wasteserviceCtx.terminateTheContext()

			ColorsOut.outappl("Gui tests done!", ColorsOut.GREEN)
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

			wasteserviceCtx.addActor(trolleyDummyActor)
			wasteserviceCtx.addActor(wasteserviceDummyActor)
			wasteserviceCtx.addActor(storageDummyActor)
			ColorsOut.outappl("WasteService: added fake actors <${trolleyDummyActor.name}> <${wasteserviceDummyActor.name}> <${storageDummyActor.name}>", ColorsOut.GREEN)

			val ledCoapServer = DummyCoapServer(FAKE_LED_PORT, "led")
			ledCoapResource = ledCoapServer.resource
			ledCoapServer.start()
			ColorsOut.outappl("WasteService: started fake led coap server $ledCoapServer", ColorsOut.GREEN)
		}
	}

	lateinit var wsConn: Interaction2021

	@Autowired
	private var controller: StatusGUIController? = null
	@LocalServerPort
	private var port: Int = 0

	@BeforeEach
	fun up() {
		CommSystemConfig.tracing = true

		// Verifica corretto avvio della webapp
		assertThat(controller).isNotNull();

		startWsConnection()
	}

	@AfterEach
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

		checkGuiResponseState(TrolleyStatus.State.WORK, "work")
		checkGuiResponseState(TrolleyStatus.State.STOPPED, "stopped")
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
				"ws://%s:%d/statusgui",
				"localhost",
				port
			)
		)
	}

	private fun checkGuiResponsePosition(input: String, expectedContent: String) {
		wasteserviceDummyActor.fakeResourceUpdate("tpos($input)")

		CommUtils.delay(500)
		val guiContent = getGuiContent()
		val tposPattern = Regex("(?<=trolleyPosition:).+", RegexOption.MULTILINE)
		val tposSegment = tposPattern.find(guiContent)?.value ?: throw IllegalStateException("Wrong response $guiContent")
		assertEquals(expectedContent, tposSegment.lowercase().trim())
	}

	private fun checkGuiResponseState(input: TrolleyStatus.State, expectedContent: String) {
		wasteserviceDummyActor.fakeResourceUpdate("tpos(indoor)")
		trolleyDummyActor.fakeResourceUpdate(
			TrolleyStatus(input, arrayOf(-1,-1), null, 0f, TrolleyStatus.Activity.IDLE)
				.toString()
		)

		CommUtils.delay(500)
		val guiContent = getGuiContent()
		val tstatePattern = Regex("(?<=trolleyState:).+", RegexOption.MULTILINE)
		val tstateSegment = tstatePattern.find(guiContent)!!.value
		assertEquals(expectedContent, tstateSegment.lowercase().trim())
	}

	private fun checkGuiResponseLed(input: String, expectedContent: String) {
		ledCoapResource.sendUpdates("ledState($input)")

		CommUtils.delay(500)
		val guiContent = getGuiContent()
		val ledPattern = Regex("(?<=ledState:).+", RegexOption.MULTILINE)
		val ledSegment = ledPattern.find(guiContent)!!.value
		assertEquals(expectedContent, ledSegment.lowercase().trim())
	}

	private fun checkGuiResponseStorage(input: Int, expectedContent: Int) {
		// Checks against glass, for simplicity
		storageDummyActor.fakeResourceUpdate("content(glass,$input,50)\ncontent(plastic,0,50)")

		CommUtils.delay(500)
		val guiContent = getGuiContent()
		val glassPattern = Regex("(?<=depositedGlass:).+", RegexOption.MULTILINE)
		val glassSegment = glassPattern.find(guiContent)!!.value
		assertEquals(expectedContent, glassSegment.trim().toFloat().toInt())
	}

	private fun getGuiContent(): String {
		// Clean queue
		while ((wsConn as WsConnSpring).hasMessages()) {
			wsConn.receiveMsg()
		}

		// Returns updates from all
		wsConn.forward("get")
		// Repeat 6 times to get all 6 updates, chain them for elaboration
		var sb = StringBuilder()
		repeat(6) {
			sb.append(wsConn.receiveMsg())
			sb.append("\n")
		}
		val msg = sb.toString()
		ColorsOut.out("Received $msg from websocket", ColorsOut.BLUE)
		return msg
	}

	internal class DummyActor(name: String) : ActorBasic(name) {
		override suspend fun actorBody(msg: IApplMessage) {
		}

		fun fakeResourceUpdate(data: String) {
			updateResourceRep(data)
			ColorsOut.outappl("DummyActor | ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())} Faked $name update: $data", ColorsOut.ANSI_PURPLE)
		}
	}

	internal class DummyCoapServer(val port: Int, val resourceName: String) {
		val server = CoapServer(port)
		val resource = DummyCoapResource(resourceName)

		init {
			server.add(resource)
		}

		fun start() {
			server.start()
			ColorsOut.out("Started DummyCoapServer $resourceName, port $port", ColorsOut.BLUE)
		}

		fun stop() {
			server.stop()
			ColorsOut.out("Stopped DummyCoapServer $resourceName, port $port", ColorsOut.BLUE)
		}
	}

	internal class DummyCoapResource(name: String) : CoapResource(name) {
		init {
			isObservable = true
		}
		var value = ""

		fun sendUpdates(value: String) {
			this.value = value
			changed()
			ColorsOut.outappl("DummyCoapResource | ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())} Faked $name update: $value", ColorsOut.ANSI_PURPLE)
		}

		override fun handleGET(exchange: CoapExchange) {
			exchange.respond(value)
		}

		override fun handlePOST(exchange: CoapExchange) = exchange.respond( "POST not implemented")
		override fun handlePUT(exchange: CoapExchange) = exchange.respond( "PUT not implemented")
		override fun handleDELETE(exchange: CoapExchange) = exchange.respond( "DELETE not allowed")
	}
}