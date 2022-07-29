package it.unibo.lenziguerra.wasteservice.utils

import it.unibo.kactor.MsgUtil
import it.unibo.lenziguerra.wasteservice.SystemConfig
import unibo.actor22comm.utils.ColorsOut

fun sendDispatch(id: String, params: String, actor: String) {
    val message: String = MsgUtil.buildDispatch("test", id, "$id($params)", actor).toString()
    try {
        val connTcp = ConnTcp(SystemConfig.hosts[actor], SystemConfig.ports[actor]!!)
        ColorsOut.outappl("Sending dispatch to $actor: $id($params)", ColorsOut.CYAN)
        connTcp.forward(message)
        connTcp.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun sendRequest(id: String, params: String, actor: String) {
    val request: String = MsgUtil.buildRequest(
        "test", id, "$id($params)", actor
    ).toString()
    try {
        val connTcp = ConnTcp(SystemConfig.hosts[actor], SystemConfig.ports[actor]!!)
        ColorsOut.outappl("Asking $actor: $id($params)", ColorsOut.CYAN)
        connTcp.request(request)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}