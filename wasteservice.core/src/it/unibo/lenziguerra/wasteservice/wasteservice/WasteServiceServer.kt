package it.unibo.lenziguerra.wasteservice.wasteservice

import it.unibo.kactor.ApplMessage
import it.unibo.kactor.MsgUtil
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.data.TrolleyStatus
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import org.springframework.beans.factory.annotation.Autowired
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
import unibo.comm22.coap.CoapConnection
import unibo.comm22.interfaces.Interaction2021
import unibo.comm22.tcp.TcpClientSupport
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import java.net.SocketException


const val STORAGE_REQ_ID = "storageAsk"
const val DEPOSIT_TRIGGER_ID = "triggerDeposit"

const val SENDER_WS_SERVER = "wasteservice_server"

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
            "types" to WasteType.values()
                .map { it -> mapOf("id" to it.id, "name" to it.id.replaceFirstChar { it.uppercase() }) },
        )
        return "truck_gui"
    }
}

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    @Autowired
    var websocketHandler: TruckWebsocketHandler? = null

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(websocketHandler!!, "/truck")
    }
}

@Component
class TruckWebsocketHandler : TextWebSocketHandler() {
    lateinit var storageReqConn: Interaction2021
    lateinit var ctxConnection: Interaction2021

    init {
        ColorsOut.out("Initialized TruckWebsocketHandler!", ColorsOut.BLUE)
    }

    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        try {
            val id = PrologUtils.extractId(payload)
            val args = PrologUtils.extractPayload(payload)
            val depositType = args[0]
            val depositAmount = args[1].toFloat()

            if (WasteType.values().find { it.id == depositType } == null) {
                ColorsOut.outerr("Illegal request received for unknown material $depositType")
                return
            }

            if (SystemConfig.debugPrint) {
                ColorsOut.outappl("Message arrived: $payload", ColorsOut.BLUE)
                ColorsOut.outappl("Id: $id, Type: $depositType, Args: $depositAmount", ColorsOut.ANSI_PURPLE)
            }

            if (!this::storageReqConn.isInitialized) {
                storageConnect()
            }

            val storageReqMessage = MsgUtil.buildRequest(
                SENDER_WS_SERVER, STORAGE_REQ_ID, PrologUtils.build(STORAGE_REQ_ID, args[0]), SystemConfig.actors["storage"]!!
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

            val trolleyStatus = TrolleyStatus.fromProlog(coapRequest("trolley"))
            val trolleyAmount = if (trolleyStatus.contentType == WasteType.valueOf(depositType.uppercase()))
                trolleyStatus.contentAmount else 0f

            if (depositAmount + trolleyAmount <= freeSpace) {
                session.sendMessage(TextMessage("loadaccept"))
                sendTrolley(session, depositType, depositAmount)
            } else {
                session.sendMessage(TextMessage("loadrejected"))
            }
        } catch (e: Exception) {
            ColorsOut.outerr(e.message)
            e.printStackTrace()
        }
    }

    fun sendTrolley(session: WebSocketSession, depositType: String, depositAmount: Float) {
        if (!this::ctxConnection.isInitialized) {
            ctxConnect()
        }

        val contextTrigger = MsgUtil.buildRequest(
            SENDER_WS_SERVER,
            DEPOSIT_TRIGGER_ID,
            PrologUtils.build(DEPOSIT_TRIGGER_ID, depositType, depositAmount.toString()),
            SystemConfig.actors["wasteServiceContext"]!!
        )

        // Bloccante fino a raccolta da trolley
        val contextReply: String = try {
            ctxConnection.request(contextTrigger.toString())
        } catch (e: SocketException) {
            ColorsOut.out("Lost connection to wasteservice context, trying to reconnect...", ColorsOut.YELLOW)
            ctxConnect()
            ctxConnection.request(contextTrigger.toString())
        }

        ColorsOut.out("Trash collect done, message was $contextReply")
        ColorsOut.out("Sending pickup message to waste truck on session $session...")
        session.sendMessage(TextMessage("pickedUp"))
    }

    private fun storageConnect() {
        storageReqConn = TcpClientSupport.connect(SystemConfig.hosts["storage"]!!, SystemConfig.ports["storage"]!!, 5)
    }

    private fun ctxConnect() {
        ctxConnection = TcpClientSupport.connect("localhost", SystemConfig.ports["wasteServiceContext"]!!, 5)
    }

    protected fun coapRequest(target: String): String {
        val reqConn = CoapConnection(
            SystemConfig.hosts[target] + ":" + SystemConfig.ports[target],
            "${SystemConfig.contexts[target]}/${SystemConfig.actors[target]}"
        )
        val answer = reqConn.request("")
        ColorsOut.out("coapRequest answer=$answer", ColorsOut.BLUE)
        return answer
    }
}