package it.unibo.lenziguerra.wasteservice.utils

import unibo.comm22.interfaces.Interaction2021
import unibo.comm22.tcp.TcpClientSupport
import unibo.comm22.utils.ColorsOut

class ConnTcp(hostAddr: String?, port: Int) : Interaction2021 {
    private val conn: Interaction2021

    init {
        conn = TcpClientSupport.connect(hostAddr, port, 10)
        ColorsOut.out("ConnTcp createConnection DONE:$conn", ColorsOut.GREEN)
    }

    override fun forward(msg: String) {
        try {
            //ColorsOut.outappl("ConnTcp forward:" + msg   , ColorsOut.GREEN);
            conn.forward(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    override fun request(msg: String): String {
        forward(msg)
        return receiveMsg()
    }

    @Throws(Exception::class)
    override fun reply(s: String) {
        forward(s)
    }

    @Throws(Exception::class)
    override fun receiveMsg(): String {
        return conn.receiveMsg()
    }

    @Throws(Exception::class)
    override fun close() {
        conn.close()
    }

    @Throws(Exception::class)
    override fun sendALine(s: String) {
        conn.sendALine(s)
    }

    @Throws(Exception::class)
    override fun sendALine(s: String, b: Boolean) {
        conn.sendALine(s, b)
    }

    @Throws(Exception::class)
    override fun receiveALine(): String {
        return conn.receiveALine()
    }

    @Throws(Exception::class)
    override fun closeConnection() {
        conn.closeConnection()
    }
}