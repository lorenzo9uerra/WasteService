package it.unibo.kactor.observer

import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.MsgUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.runBlocking
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import unibo.comm22.utils.ColorsOut
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class CoapObserverActor(private val resourceName: String, val owner: ActorBasic) : CoapHandler {
    override fun onLoad(response: CoapResponse?) {
        // Ignore initial response
        if (isInitialResponse(response))
            return

        // Mainly to avoid owner getting terminated (e.g. in tests)
        // inbetween the run of this method
        ColorsOut.out("CoapObserverActor | ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())} ${owner.name} received update from $resourceName, responseText: ${response?.responseText}", ColorsOut.BLACK)

        val responseText = response!!.responseText
        // Newlines break TuProlog, use arbitrary encoding that can later be replaced with \n in usecases
        val cleanedResponseText = responseText.replace("\n", "%%&NL%%")
        val actorDispatch = MsgUtil.buildDispatch(
            resourceName,
            "coapUpdate",
            "coapUpdate('$resourceName', '$cleanedResponseText')",
            owner.name,
        )
        try {
            owner.sendMsgToMyself(actorDispatch)
            ColorsOut.out("CoapObserverActor | ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())} ${owner.name}/$resourceName: sent update, is $actorDispatch", ColorsOut.BLACK)
        } catch (e: ClosedSendChannelException) {
            ColorsOut.outerr("CoapObserverActor | tried to send to closed channel, actor stopped in another thread? Exception: ${e.message}")
        }
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