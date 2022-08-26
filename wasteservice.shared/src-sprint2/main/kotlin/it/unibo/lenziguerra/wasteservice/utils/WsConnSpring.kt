package it.unibo.lenziguerra.wasteservice.utils

import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import unibo.comm22.interfaces.Interaction2021
import unibo.comm22.utils.ColorsOut
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class WsConnSpring(val uri: String) : Interaction2021 {
    val msgQueue : BlockingQueue<String> = LinkedBlockingQueue()
    var justErrored = false
    private val session: WebSocketSession

    init {
        val cln: WebSocketClient = StandardWebSocketClient()
        session = cln.doHandshake(InnerWebsocketHandler(), uri).get()
    }

    inner class InnerWebsocketHandler : WebSocketHandler {
        override fun afterConnectionEstablished(session: WebSocketSession) {
            ColorsOut.out(this.javaClass.name + " | established connection with $uri", ColorsOut.BLUE)
        }

        override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
            ColorsOut.out(this.javaClass.name + " | received ${message.payload}", ColorsOut.BLACK)
            msgQueue.put(message.payload.toString())
        }

        override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
            ColorsOut.outerr(this.javaClass.name + " | error: $exception")
            exception.printStackTrace()
            justErrored = true
        }

        override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
            ColorsOut.out(this.javaClass.name + " | closed connection with $uri", ColorsOut.BLUE)
        }

        override fun supportsPartialMessages(): Boolean = false
    }

    override fun forward(msg: String) {
        session.sendMessage(TextMessage(msg))
        ColorsOut.out(this.javaClass.name + " | sent $msg", ColorsOut.BLACK)
    }

    override fun request(msg: String): String {
        forward(msg)
        return receiveMsg()
    }

    override fun reply(msg: String) = forward(msg)

    override fun receiveMsg(): String = msgQueue.take()

    override fun close() = session.close()

    fun hasMessages() = !msgQueue.isEmpty()
}