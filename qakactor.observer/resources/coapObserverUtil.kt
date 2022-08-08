import it.unibo.kactor.ActorBasic
import it.unibo.kactor.QakContext
import it.unibo.kactor.observer.CoapObserverActor
import it.unibo.kactor.sysUtil
import unibo.comm22.coap.CoapConnection
import unibo.comm22.interfaces.Interaction2021

// Smaller case class name because used by Qak
object coapObserverUtil {
    private val actorResourceConnections: MutableMap<Pair<ActorBasic, String>, Interaction2021>
        = mutableMapOf()

    /**
     * Run inside qak file, passing myself as first argument
     * and target context and actor name after.
     * Will pass dispatches to the actor formatted like coapUpdate(VALUE),
     * where VALUE is what the observed resource returns.
     * Needs to define `dispatch coapUpdate : coapUpdate(VALUE)` inside the
     * qak file.
     */
    fun startObserving(actor: ActorBasic, contextName: String, actorName: String) {
        val context = sysUtil.getContext(contextName) ?:
            throw IllegalArgumentException("Unknown context $contextName")

        startObservingHost(
            actor,
            "${context.hostAddr}:${context.portNum}",
            "$contextName/$actorName",
        )
    }

    /**
     * Same as `startObserving`, but automatically use
     * local context for convenience.
     */
    fun startObserving(actor: ActorBasic, actorName: String) {
        val context = actor.context ?:
        throw IllegalArgumentException("Actor doesn't have a context")

        startObservingHost(
            actor,
            "${context.hostAddr}:${context.portNum}",
            "${context.name}/$actorName",
        )
    }

    /**
     * Same as `startObserving`, but specify hostname and port, and resource uri
     * manually.
     */
    fun startObservingHost(actor: ActorBasic, resourceHost: String, resourceUri: String) {
        val key = Pair(actor, "$resourceHost/$resourceUri")
        if (actorResourceConnections.containsKey(key)) {
            throw IllegalArgumentException("Connection for $key already exists")
        }

        val connection = CoapConnection(resourceHost, resourceUri)
        connection.observeResource(CoapObserverActor(resourceUri.replace("/", "."), actor))
        actorResourceConnections[key] = connection
    }

    fun stopObserving(actor: ActorBasic, resourceHost: String, resourceUri: String) {
        val key = Pair(actor, "$resourceHost/$resourceUri")
        actorResourceConnections[key]?.close()
        actorResourceConnections.remove(key)
    }
}