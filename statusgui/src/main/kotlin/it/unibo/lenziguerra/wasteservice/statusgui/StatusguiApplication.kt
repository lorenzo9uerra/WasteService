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
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import unibo.actor22comm.coap.CoapConnection
import unibo.actor22comm.interfaces.Interaction2021
import unibo.actor22comm.utils.ColorsOut

const val STORAGE_REQ_ID = "storageAsk"
const val DEPOSIT_TRIGGER_ID = "triggerDeposit"

const val ACTOR_STORAGE_MANAGER = "storagemanager"
const val ACTOR_WASTESERVICE = "wasteservice"
const val SENDER_WS_SERVER = "wasteservice_server"

@SpringBootApplication
class StatusguiApplication


fun main(args: Array<String>) {
    runApplication<StatusguiApplication>(*args)
}

@Controller
class WasteServiceController {
    @GetMapping("/")
    fun truckGui(model: Model): String {
        model["title"] = "WasteServiceStatusGUI"
        model["scripts"] = arrayOf(
            "/jquery-3.6.0.min.js",
            "/statusgui.js",
        )
        return "statusgui"
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
    private final var ledObserver= LedObserver(wsList)
    private final var wasteServiceObserver= WasteServiceObserver(wsList)



    lateinit var ctxConnection: Interaction2021

    init {
        startCoapConnection("trolley", trolleyObserver)
        startCoapConnection("storage", storageObserver)
        startCoapConnection("led", ledObserver)
        startCoapConnection("wasteservice", wasteServiceObserver)
        ColorsOut.out("Initialized StatusGuiWebsocketHandler!", ColorsOut.BLUE)
    }


    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        wsList.add(session)
    }

    private final fun startCoapConnection(actor: String, observer: CoapHandler) {
        Thread {
            val conn = CoapConnection(
                SystemConfig.hosts[actor]
                        + ":" + SystemConfig.ports[actor],
                SystemConfig.contexts[actor] + "/" + actor
            )
            conn.observeResource(observer)
            ColorsOut.outappl("connected via Coap conn:$conn", ColorsOut.CYAN)
        }.start()
    }
}