package it.unibo.lenziguerra.wasteservice.wasteservice

import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
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
import unibo.actor22comm.utils.ColorsOut
import it.unibo.lenziguerra.wasteservice.SystemConfig


@Controller
class WasteServiceController {
    @GetMapping("/")
    fun truckGui(model: Model): String {
        model["title"] = "WasteService"
        model["scripts"] = arrayOf(
            "/jquery-3.6.0.min.js",
            "/truckgui.js",
        );
        model["waste"] = mapOf(
          "types" to WasteType.values().map { it -> mapOf("id" to it.id, "name" to it.id.replaceFirstChar { it.uppercase() }) },
        );
        return "truck_gui"
    }
}

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(TruckWebsocketHandler(), "/truck")
    }
}

@Component
class TruckWebsocketHandler : TextWebSocketHandler() {
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        println("test $payload")
        try {
            val id = PrologUtils.extractId(payload);
            val args = PrologUtils.extractPayload(payload);

            if (SystemConfig.debugPrint) {
                ColorsOut.outappl("Message arrived: $payload", ColorsOut.BLUE)
                ColorsOut.outappl("Id: $id, Args: $args", ColorsOut.ANSI_PURPLE)
            }

            //        session.sendMessage(TextMessage("$payload reply at ${LocalTime.now()}"))
        } catch (e: Exception) {
            ColorsOut.outerr(e.message)
            e.printStackTrace()
        }
    }
}