package it.unibo.lenziguerra.wasteservice.utils

import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import unibo.actor22comm.interfaces.Interaction2021
import unibo.actor22comm.utils.ColorsOut
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class WsConnRequestable(val uri: String) : Interaction2021 {
    val msgQueue : BlockingQueue<String> = LinkedBlockingQueue()
    var justErrored = false
    private val session: WebSocketSession

    init {
        val cln: WebSocketClient = StandardWebSocketClient()
        session = cln.doHandshake(InnerWebsocketHandler(), uri).get()
    }

    inner class InnerWebsocketHandler : WebSocketHandler {
        override fun afterConnectionEstablished(session: WebSocketSession) {
            ColorsOut.out("WsConnRequestable | established connection with $uri", ColorsOut.BLUE)
        }

        override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
            ColorsOut.out("WsConnRequestable | received $message", ColorsOut.BLACK)
            msgQueue.put(message.payload.toString())
        }

        override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
            ColorsOut.outerr("WsConnRequestable | error: $exception")
            exception.printStackTrace()
            justErrored = true
        }

        override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
            ColorsOut.out("WsConnRequestable | closed connection with $uri", ColorsOut.BLUE)
        }

        override fun supportsPartialMessages(): Boolean = false
    }

    override fun forward(msg: String) {
        session.sendMessage(TextMessage(msg))
        ColorsOut.out("WsConnRequestable | sent $msg", ColorsOut.BLACK)
    }

    override fun request(msg: String): String {
        forward(msg)
        return receiveMsg()
    }

    override fun reply(msg: String) = forward(msg)

    override fun receiveMsg(): String = msgQueue.take()

    override fun sendALine(msg: String) = forward(msg)

    override fun sendALine(msg: String, isAnswer: Boolean) = sendALine(msg)

    override fun receiveALine(): String = receiveMsg()

    override fun close() = session.close()

    override fun closeConnection() = close()
}