package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.ApplMessage
import it.unibo.kactor.MsgUtil
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import unibo.actor22comm.interfaces.Interaction2021
import unibo.actor22comm.tcp.TcpClientSupport
import unibo.actor22comm.utils.CommSystemConfig
import java.net.SocketException


@Controller
class WasteServiceController {
    init {
        CommSystemConfig.tracing = true
    }

    @GetMapping("/")
    fun truckGui(model: Model): String {
        model["title"] = "WasteService"
        model["scripts"] = arrayOf(
            "/jquery-3.6.0.min.js",
            "/truckgui.js",
        )
        model["waste"] = mapOf(
          "types" to WasteType.values().map { it -> mapOf("id" to it.id, "name" to it.id.replaceFirstChar { it.uppercase() }) },
        )
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
    lateinit var storageReqConn: Interaction2021

    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        println("test $payload")
        try {
            val id = PrologUtils.extractId(payload)
            val args = PrologUtils.extractPayload(payload)
            val depositType = args[0]
            val depositAmount = args[1].toFloat()

            if (SystemConfig.debugPrint) {
                ColorsOut.outappl("Message arrived: $payload", ColorsOut.BLUE)
                ColorsOut.outappl("Id: $depositType, Args: $depositAmount", ColorsOut.ANSI_PURPLE)
            }

            if (!this::storageReqConn.isInitialized) {
                storageConnect()
            }

            val storageManagerId = "storagemanager"
            val storageReqMessage = MsgUtil.buildRequest(
//                "wasteservice_server",
                "test",
                "storageAsk",
                PrologUtils.build("storageAsk", args[0]),
                storageManagerId
            )

            val storageReply: String = try {
                storageReqConn.request(storageReqMessage.toString())
            } catch (e: SocketException) {
                ColorsOut.out("Lost connection to storage, trying to reconnect...", ColorsOut.YELLOW)
                storageConnect()
                storageReqConn.request(storageReqMessage.toString())
            }

            val replyMessage = ApplMessage(storageReply)
            val freeSpace = PrologUtils.extractPayload(replyMessage.msgContent())[1].toFloat()

            // TODO: Aggiungi anche la quantità trasportata dal Trolley

            if (depositAmount <= freeSpace) {
                session.sendMessage(TextMessage("loadaccept"))

                sendTrolley(session)
            } else {
                session.sendMessage(TextMessage("loadrejected"))
            }
        } catch (e: Exception) {
            ColorsOut.outerr(e.message)
            e.printStackTrace()
        }
    }

    fun sendTrolley(session: WebSocketSession) {
        runBlocking {
            delay(1000)
            ColorsOut.out("Sending pickup message to waste truck on session $session...")
            session.sendMessage(TextMessage("pickUp"))
        }
    }

    private fun storageConnect() {
        storageReqConn = TcpClientSupport.connect(SystemConfig.storageHost, SystemConfig.storagePort, 5)
    }
}