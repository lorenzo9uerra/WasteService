package it.unibo.kactor.observer

import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.MsgUtil
import kotlinx.coroutines.runBlocking
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import unibo.comm22.utils.ColorsOut

class CoapObserverActor(private val resourceName: String, val owner: ActorBasic) : CoapHandler {
    override fun onLoad(response: CoapResponse?) {
        ColorsOut.out("CoapObserverActor | ${owner.name} received update, responseText: ${response?.responseText}", ColorsOut.BLACK)

        val responseText = response!!.responseText
        val actorDispatch = MsgUtil.buildDispatch(
            resourceName,
            "coapUpdate",
            "coapUpdate('$responseText')",
            owner.name,
        )
        owner.sendMsgToMyself(actorDispatch)
    }

    override fun onError() {
        ColorsOut.outerr("CoapObserverActor | Communication error!")
    }
}