package it.unibo.lenziguerra.wasteservice.sonar

import it.unibo.kactor.QakContext
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig
import kotlinx.coroutines.runBlocking
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import java.io.File
import java.io.FileWriter
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.io.path.deleteIfExists

fun main() {
    SystemConfig.setConfiguration()
    DomainSystemConfig.setTheConfiguration("SonarConfig.json")
    CommSystemConfig.tracing = SystemConfig.debugPrint

    val thisContext = SystemConfig.contexts["sonar"]!!
    val contextsFile = SonarContextHelper.createContextDefinition("wasteservice_sonar.pl", thisContext)

    Runtime.getRuntime().addShutdownHook(thread(start=false, block={
        SonarContextHelper.deletePreviousDefinitionFile()
        if (SystemConfig.debugPrint)
            println("Cleaned temporary files")
    }))

    runBlocking {
        QakContext.createContexts("localhost", this, contextsFile, "sysRules.pl", thisContext)
    }
}

object SonarContextHelper {
    lateinit var contextDefTempFile: Path

    /*
    context(ctx_wasteservice_sonar, "localhost",  "TCP", "8031").
    context(ctx_wasteservice, "host.trolley",  "TCP", "8023").
     qactor( sonar_shim, ctx_wasteservice_sonar, "it.unibo.lenziguerra.wasteservice.sonar.SonarShim").
     */

    fun createContextDefinition(baseQakDefFilepath: String, thisContextName: String): String {
        val thisContextPattern = Regex(
        "context\\($thisContextName,\\s*\"localhost\"" +
            "\\s*,\\s*\"(?<prot>[^\"]+)\"\\s*,\\s*\"(?<port>[^\"]+)\""
        )
        val ctxComponentPattern = Regex(
        "context\\((?<context>\\w+),\\s*\"host.(?<component>\\w+)\"" +
            "\\s*,\\s*\"(?<prot>[^\"]+)\"\\s*,\\s*\"(?<port>[^\"]+)\""
        )
        val qactorPattern = Regex("qactor\\(")
        var actorContextsBase: MutableMap<String, String> = mutableMapOf()

        val replacedContent = File(baseQakDefFilepath).readLines().map {
            if (thisContextPattern.containsMatchIn(it)) {
                thisContextPattern.replace(it,
                    "context($thisContextName, \"localhost\", " +
                    "\"\${prot}\", \"${SystemConfig.ports["sonar"]}\""
                )
            } else if (ctxComponentPattern.containsMatchIn(it)) {
                val match = ctxComponentPattern.find(it)!!
                val component = match.groups["component"]!!.value
                val host = if (SystemConfig.hosts[component] == "localhost")
                    "127.0.0.1" else SystemConfig.hosts[component]!!
                actorContextsBase[component] = match.groups["context"]!!.value
                ctxComponentPattern.replace(it,
                    "context(${SystemConfig.contexts[component]}, " +
                    "\"$host\", \"\${prot}\", \"${SystemConfig.ports[component]}\""
                )
            } else {
                var str = it
                actorContextsBase.forEach { entry ->
                    str = str.replace(entry.value, SystemConfig.contexts[entry.key]!!)
                }
                str
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