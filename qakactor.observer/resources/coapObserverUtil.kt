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
     * Will pass dispatches to the actor formatted like coapUpdate(RESOURCE, VALUE),
     * where RESOURCE is the resource name, and VALUE is what the observed resource returns.
     * Needs to define `dispatch coapUpdate : coapUpdate(RESOURCE, VALUE)` inside the
     * qak file.
     */
    fun startObserving(actor: ActorBasic, contextName: String, actorName: String) {
        val context = sysUtil.getContext(contextName) ?:
            throw IllegalArgumentException("Unknown context $contextName")

        startObservingBase(
            actor,
            "${context.hostAddr}:${context.portNum}",
            "$contextName/$actorName",
            actorName
        )
    }

    /**
     * Same as `startObserving`, but automatically use
     * local context for convenience.
     */
    fun startObserving(actor: ActorBasic, actorName: String) {
        val context = actor.context ?:
        throw IllegalArgumentException("Actor doesn't have a context")

        startObservingBase(
            actor,
            "${context.hostAddr}:${context.portNum}",
            "${context.name}/$actorName",
            actorName
        )
    }

    /**
     * Same as `startObserving`, but specify hostname and port, and resource uri
     * manually.
     */
    fun startObservingHost(actor: ActorBasic, resourceHost: String, resourceUri: String) {
        startObservingBase(actor, resourceHost, resourceUri, resourceUri.replace("/", "_"))
    }

    private fun startObservingBase(actor: ActorBasic, resourceHost: String, resourceUri: String, resourceName: String) {
        val key = Pair(actor, "$resourceHost/$resourceUri")
        if (actorResourceConnections.containsKey(key)) {
            throw IllegalArgumentException("Connection for $key already exists")
        }

        val connection = CoapConnection(resourceHost, resourceUri)
        connection.observeResource(CoapObserverActor(resourceName, actor))
        actorResourceConnections[key] = connection
    }

    fun stopAllObserving(actor: ActorBasic) {
        actorResourceConnections.keys.removeIf { key ->
            val out = key.first == actor
            if (out) {
                actorResourceConnections[key]!!.close()
            }
            out
        }
    }

    fun stopObserving(actor: ActorBasic, contextName: String, actorName: String) {
        val context = sysUtil.getContext(contextName) ?:
            throw IllegalArgumentException("Unknown context $contextName")

        stopObservingHost(
            actor,
            "${context.hostAddr}:${context.portNum}",
            "${context.name}/$actorName",
        )
    }
    fun stopObserving(actor: ActorBasic, actorName: String) {
        val context = actor.context ?:
            throw IllegalArgumentException("Actor doesn't have a context")

        stopObservingHost(
            actor,
            "${context.hostAddr}:${context.portNum}",
            "${context.name}/$actorName",
        )
    }

    fun stopObservingHost(actor: ActorBasic, resourceHost: String, resourceUri: String) {
        val key = Pair(actor, "$resourceHost/$resourceUri")
        actorResourceConnections[key]?.close()
        actorResourceConnections.remove(key)
    }
}