package unibo.lenziguerra.wasteservice.raspgui

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.lenziguerra.wasteservice.led.BlinkLed
import it.unibo.lenziguerra.wasteservice.led.LedContainer
import it.unibo.lenziguerra.wasteservice.sonar.SonarShim
import it.unibo.radarSystem22.domain.DeviceFactory
import it.unibo.radarSystem22.domain.Distance
import it.unibo.radarSystem22.domain.interfaces.ILed
import it.unibo.radarSystem22.domain.interfaces.ISonar
import it.unibo.radarSystem22.domain.models.SonarModel
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import java.util.concurrent.Semaphore
import javax.annotation.PreDestroy
import kotlin.concurrent.thread

@SpringBootApplication
class Application {
	@Autowired
	lateinit var sonarComponent: SonarComponent

	init {
		CommSystemConfig.tracing = true
		SystemConfig.setConfiguration()
		SystemConfig.disableRead()
	}
}

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}

@Controller
class RaspGuiController {
	init {
	}

	@GetMapping("/")
	fun truckGui(model: Model): String {
		model["title"] = "WasteService RaspGUI"
		model["scripts"] = arrayOf(
			"/jquery-3.6.0.min.js",
			"/raspgui.js",
		)
		model["dlimit"] = SystemConfig.DLIMIT
		return "rasp_gui"
	}
}

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
	@Autowired
	lateinit var websocketHandler: RaspGuiWebsocketHandler

	override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
		registry.addHandler(websocketHandler!!, "/ws")
	}
}

@Component
class RaspGuiWebsocketHandler : TextWebSocketHandler() {
	@Autowired
	lateinit var webSonarMock : WebSonarMock

	val led = WebUpdateLed()
	val sessions = mutableListOf<WebSocketSession>()
	val ledContainer = LedContainer(SystemConfig.ports["led"]!!, BlinkLed(led))

	init {
		ledContainer.start()
	}

	private val setSonarPattern = Regex("(?<=setSonar\\()\\d+(?=\\))")
	private val getPattern = Regex("get\\(\\)")

	public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
		setSonarPattern.find(message.payload)?.let {
			webSonarMock.value = it.value.toInt()
		}
		if (getPattern.matches(message.payload.trim())) {
			session.sendMessage(TextMessage("ledUpdate(${led.status})"))
			session.sendMessage(TextMessage("dlimit(${SystemConfig.DLIMIT})"))
		}
	}

	override fun afterConnectionEstablished(session: WebSocketSession) {
		sessions.add(session)
	}

	override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
		sessions.remove(session)
	}

	inner class WebUpdateLed : ILed {
		var status = false

		fun sendUpdate() {
			sessions.forEach {
				synchronized(it) {
					it.sendMessage(TextMessage("ledUpdate($status)"))
				}
			}
		}

		override fun turnOn() {
			status = true
			sendUpdate()
		}

		override fun turnOff() {
			status = false
			sendUpdate()
		}

		override fun getState(): Boolean = status
	}
}

@Component
class SonarComponent @Autowired constructor(sonar: ISonar) {
	final val qakCtx: QakContext
	final val sonarShim : SonarShim

	init {
		thread {
			it.unibo.lenziguerra.wasteservice.sonar.main()
		}

		var tmpQakCtx = sysUtil.getContext(SystemConfig.contexts["sonar"]!!)
		while (tmpQakCtx == null) {
			CommUtils.delay(200)
			tmpQakCtx = sysUtil.getContext(SystemConfig.contexts["sonar"]!!)
		}
		qakCtx = sysUtil.getContext(SystemConfig.contexts["sonar"]!!)!!

		sonarShim = SonarShim(SystemConfig.actors["sonar"]!!)

		sonarShim.sonar = sonar
		sonarShim.observableSonar = DeviceFactory.makeSonarObservable(sonar)
		sonarShim.observableDistance = DeviceFactory.makeDistanceObservable(sonar)

		qakCtx.addActor(sonarShim)

		runBlocking { MsgUtil.sendMsg(
			MsgUtil.buildDispatch("raspGui", "sonarStart", "sonarStart(_)", sonarShim.name),
			sonarShim
		) }
	}

	@PreDestroy
	fun preShutdown() {
		qakCtx.terminateTheContext()
	}

}

@Component
class WebSonarMock : SonarModel() {
	private lateinit var changeSem: Semaphore
	var value: Int = DomainSystemConfig.sonarMockStartDist
		set(value) {
			changeSem.release()
			field = value
		}

	override fun sonarSetUp() {
		changeSem = Semaphore(0)
		value = DomainSystemConfig.sonarMockStartDist
		distance.set(Distance(value))
		ColorsOut.outappl("[Sonar mock web] set up", ColorsOut.CYAN)
	}

	override fun sonarProduce() {
		changeSem.acquire()
		updateDistance(value)
	}

}
