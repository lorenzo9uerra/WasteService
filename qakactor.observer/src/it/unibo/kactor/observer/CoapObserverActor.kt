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
        // Ignore initial response
        if (isInitialResponse(response))
            return

        ColorsOut.out("CoapObserverActor | ${owner.name} received update from $resourceName, responseText: ${response?.responseText}", ColorsOut.BLACK)

        val responseText = response!!.responseText
        // Newlines break TuProlog, use arbitrary encoding that can later be replaced with \n in usecases
        val cleanedResponseText = responseText.replace("\n", "%%&NL%%")
        val actorDispatch = MsgUtil.buildDispatch(
            resourceName,
            "coapUpdate",
            "coapUpdate('$resourceName', '$cleanedResponseText')",
            owner.name,
        )
        owner.sendMsgToMyself(actorDispatch)
    }

    override fun onError() {
        ColorsOut.outerr("CoapObserverActor | Communication error!")
    }

    private fun isInitialResponse(response: CoapResponse?): Boolean {
        return response != null &&
                response.responseText.trim().startsWith("ActorBasic(Resource)") &&
                response.responseText.lowercase().contains("created")
    }
}