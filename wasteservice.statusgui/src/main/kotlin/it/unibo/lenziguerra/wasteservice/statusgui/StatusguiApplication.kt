package it.unibo.lenziguerra.wasteservice.statusgui

import it.unibo.lenziguerra.wasteservice.*
import org.eclipse.californium.core.CoapHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import unibo.comm22.coap.CoapConnection
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig

@SpringBootApplication
class StatusguiApplication {
    init {
        CommSystemConfig.tracing = true
    }
}


fun main(args: Array<String>) {
    runApplication<StatusguiApplication>(*args)
}

@Controller
class StatusGUIController {
    @GetMapping("/")
    fun statusGUI(model: Model): String {
        model["title"] = "WasteServiceStatusGUI"
        model["scripts"] = arrayOf(
            "/jquery-3.6.0.min.js",
            "/statusgui.js",
        )
        return "status_gui"
    }
}

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(StatusGuiWebsocketHandler(), "/statusgui")
    }
}

@Component
class StatusGuiWebsocketHandler : TextWebSocketHandler() {
    private final var wsList = ArrayList<WebSocketSession>()
    private final var trolleyObserver = TrolleyObserver(wsList)
    private final var storageObserver = StorageObserver(wsList)
    private final var ledObserver = LedObserver(wsList)
    private final var wasteServiceObserver = WasteServiceObserver(wsList)

    init {
        startCoapConnection("trolley", trolleyObserver)
        startCoapConnection("storage", storageObserver)
        startLedCoapConnection(ledObserver)
        startCoapConnection("wasteServiceContext", wasteServiceObserver)
        ColorsOut.out("Initialized StatusGuiWebsocketHandler!", ColorsOut.BLUE)
    }


    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // Manual update, first time
        if (message.payload == "get") {
            session.sendMessage(TextMessage("trolleyState: ${trolleyObserver.lastState}"))
            session.sendMessage(TextMessage("depositedGlass: ${storageObserver.lastGlass}"))
            session.sendMessage(TextMessage("depositedPlastic: ${storageObserver.lastPlastic}"))
            session.sendMessage(TextMessage("trolleyPosition: ${wasteServiceObserver.lastPos}"))
            session.sendMessage(TextMessage("ledState: ${ledObserver.lastState}"))
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        super.afterConnectionEstablished(session)
        wsList.add(session)
        ColorsOut.out("New session started: $session", ColorsOut.ANSI_PURPLE)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        super.afterConnectionClosed(session, status)
        wsList.remove(session)
        ColorsOut.out("Session closed: $session, status: $status", ColorsOut.ANSI_PURPLE)
    }

    private fun startCoapConnection(id: String, observer: CoapHandler) {
        Thread {
            val conn = CoapConnection(
                SystemConfig.hosts[id]
                        + ":" + SystemConfig.ports[id],
                SystemConfig.contexts[id] + "/" + SystemConfig.actors[id]
            )
            conn.observeResource(observer)
            ColorsOut.outappl("connected via Coap conn:$conn", ColorsOut.CYAN)
        }.start()
    }

    private fun startLedCoapConnection(observer: CoapHandler) {
        val actor = "led"
        Thread {
            val conn = CoapConnection(
                SystemConfig.hosts[actor]
                        + ":" + SystemConfig.ports[actor],
                actor
            )
            conn.observeResource(observer)
            ColorsOut.outappl("connected via Coap conn:$conn", ColorsOut.CYAN)
        }.start()
    }
}