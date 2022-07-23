package it.unibo.lenziguerra.wasteservice

import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

class SimplePayloadExtractor(private val id: String) {
    fun extractPayload(str: String): List<String> {
        val pattern = Pattern.compile("$id\\(([^)]*)\\)")
        val matcher = pattern.matcher(str)
        var payload: String? = null
        if (matcher.matches()) {
            payload = matcher.group(1)
            if (payload == null) {
                val emptyMatcher = Pattern.compile("$id\\(\\s*\\)").matcher(str)
                require(emptyMatcher.matches()) { "Message not properly formatted: expected $id(X, Y...), got $str" }
            }
        } else {
            throw IllegalArgumentException("Message not properly formatted: expected $id(X, Y...), got $str")
        }
        val out = Arrays.stream(payload.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()).collect(Collectors.toList())
        return if (out.size == 1 && out[0] == "") {
            ArrayList()
        } else {
            out
        }
    }
}