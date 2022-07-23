package it.unibo.lenziguerra.wasteservice.utils

import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

object PrologUtils {
    fun getFuncLines(lines: String, func: String?): List<String> {
        val linesArray = lines.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Arrays.stream(linesArray).filter { l: String ->
            l.startsWith(
                func!!
            )
        }.collect(Collectors.toList())
    }

    fun getFuncLine(lines: String, func: String?): String? {
        val linesL = getFuncLines(lines, func)
        return if (linesL.isNotEmpty()) linesL[0] else null
    }

    fun extractId(str: String): String {
        val split = str.split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (split.isNotEmpty()) return split[0]
        throw IllegalArgumentException("Message not properly formatted: expected <word>(X, Y...), got $str")
    }

    fun extractPayload(str: String): List<String> {
        val pattern = Pattern.compile("\\w+\\(([^\\)]*)\\)")
        val matcher = pattern.matcher(str)
        var payload: String? = null
        if (matcher.matches()) {
            payload = matcher.group(1)
            if (payload == null) {
                val emptyMatcher = Pattern.compile("\\w+\\(\\s*\\)").matcher(str)
                require(emptyMatcher.matches()) { "Message not properly formatted: expected <word>(X, Y...), got $str" }
            }
        } else {
            throw IllegalArgumentException("Message not properly formatted: expected <word>(X, Y...), got $str")
        }
        val out = Arrays.stream(payload.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()).collect(Collectors.toList())
        return if (out.size == 1 && out[0] == "") {
            ArrayList()
        } else {
            out
        }
    }

    fun build(id: String, vararg args: String): String {
        return id + "(" + Arrays.stream(args).collect(Collectors.joining(", ")) + ")"
    }
}