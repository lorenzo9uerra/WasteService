import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ActorBasicFsm
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils

object resourcePrologUtils {
    /**
     * Use inside a onMsg block for a
     * coapUpdate dispatch with prolog data as second arg
     */
    fun resourcePayloadArg(myself: ActorBasicFsm, id: String, num: Int): String {
        val resourceValue = myself.payloadArg(1).replace("%%&NL%%", "\n")
        val line = PrologUtils.getFuncLine(resourceValue, id)!!
        return PrologUtils.extractPayload(line)[num]
    }
}