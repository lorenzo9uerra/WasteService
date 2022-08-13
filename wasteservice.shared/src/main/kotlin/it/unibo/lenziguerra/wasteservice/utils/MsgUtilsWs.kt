package it.unibo.lenziguerra.wasteservice.utils

import it.unibo.kactor.IApplMessage

object MsgUtilsWs {
    /**
     * Di base, i messaggi creati da CommUtils mettono ' ' intorno
     * al payload in certi casi tramite Term.createTerm di TuProlog,
     * che al momento rompe Qak. Questa funzione converte messaggio in
     * stringa pulendo gli apici
     */
    @JvmStatic
    fun cleanMessage(msg: IApplMessage): String {
        val cleanedContent = Regex("^'(.*)'$").replace(msg.msgContent(), "$1")
        return "msg(${msg.msgId()},${msg.msgType()},${msg.msgSender()},${msg.msgReceiver()},$cleanedContent,${msg.msgNum()})"
    }
}