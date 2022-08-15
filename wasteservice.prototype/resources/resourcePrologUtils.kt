import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ActorBasicFsm
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import unibo.comm22.utils.ColorsOut
import java.time.LocalTime

object resourcePrologUtils {
    /**
     * Use inside a onMsg block for a
     * coapUpdate dispatch with prolog data as second arg
     */
    fun resourcePayloadArg(myself: ActorBasicFsm, id: String, num: Int): String {
        val resourceValue = myself.payloadArg(1).replace("%%&NL%%", "\n")
        val line = PrologUtils.getFuncLine(resourceValue, id) ?: throw IllegalStateException("Wrong payload <$resourceValue>")
        return PrologUtils.extractPayload(line)[num]
    }

    fun resourcePayloadLines(myself: ActorBasicFsm, id: String): List<String> {
        val resourceValue = myself.payloadArg(1).replace("%%&NL%%", "\n")
        return PrologUtils.getFuncLines(resourceValue, id)
    }

    fun extractPayload(line: String, num: Int): String = PrologUtils.extractPayload(line)[num]
}