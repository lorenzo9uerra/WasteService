package it.unibo.lenziguerra.wasteservice.sonar

import it.unibo.kactor.QakContext
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import java.io.File
import java.io.FileWriter
import java.io.PrintStream
import java.nio.file.Path
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread
import kotlin.io.path.deleteIfExists

fun main() {
    SystemConfig.setConfiguration()
    DomainSystemConfig.setTheConfiguration("SonarConfig.json")
    CommSystemConfig.tracing = true

    val contextsFile = SonarContextHelper.createContextDefinition("wasteservice_sonar.pl")

    Runtime.getRuntime().addShutdownHook(thread(start=false, block={
        SonarContextHelper.deletePreviousDefinitionFile()
        if (SystemConfig.debugPrint)
            println("Cleaned temporary files")
    }))

    runBlocking {
        QakContext.createContexts("localhost", this, contextsFile, "sysRules.pl")
    }
}

object SonarContextHelper {
    lateinit var contextDefTempFile: Path

    /*
    context(ctx_wasteservice_sonar, "localhost",  "TCP", "8031").
    context(ctx_wasteservice, "host.trolley",  "TCP", "8023").
     qactor( sonar_shim, ctx_wasteservice_sonar, "it.unibo.lenziguerra.wasteservice.sonar.SonarShim").
     */

    fun createContextDefinition(baseQakDefFilepath: String): String {
        val ctxSonarPattern = Regex(
        "context\\(ctx_wasteservice_sonar,\\s*\"localhost\"" +
            "\\s*,\\s*\"(?<prot>[^\"]+)\"\\s*,\\s*\"(?<port>[^\"]+)\""
        )
        val ctxTrolleyPattern = Regex(
        "context\\((?<context>\\w+),\\s*\"(?<host>host.trolley)\"" +
            "\\s*,\\s*\"(?<prot>[^\"]+)\"\\s*,\\s*\"(?<port>[^\"]+)\""
        )
        val qactorPattern = Regex("qactor\\(")
        var trolleyCtxBase: String? = null
        val trolleyHost = if (SystemConfig.hosts["trolley"] == "localhost")
            "127.0.0.1" else SystemConfig.hosts["trolley"]!!

        val replacedContent = File(baseQakDefFilepath).readLines().map {
            if (ctxSonarPattern.containsMatchIn(it)) {
                ctxSonarPattern.replace(it,
                    "context(ctx_wasteservice_sonar, \"localhost\", " +
                    "\"\${prot}\", \"${SystemConfig.ports["sonar"]}\""
                )
            } else if (ctxTrolleyPattern.containsMatchIn(it)) {
                trolleyCtxBase = ctxTrolleyPattern.find(it)!!.groups["context"]!!.value
                ctxTrolleyPattern.replace(it,
                    "context(${SystemConfig.contexts["trolley"]}, " +
                    "\"$trolleyHost\", \"\${prot}\", \"${SystemConfig.ports["trolley"]}\""
                )
            } else if (trolleyCtxBase != null) {
                it.replace(trolleyCtxBase!!, SystemConfig.contexts["trolley"]!!)
            } else {
                it
            }
        }.joinToString("\n")

        if (SystemConfig.debugPrint) {
            println("Created context with description:")
            println(replacedContent)
        }

        contextDefTempFile = kotlin.io.path.createTempFile("sonarctx", ".pl")
        FileWriter(contextDefTempFile.toAbsolutePath().toString()).use {
            it.write(replacedContent)
        }

        return contextDefTempFile.toAbsolutePath().toString()
    }

    fun deletePreviousDefinitionFile() {
        contextDefTempFile.deleteIfExists()
    }
}