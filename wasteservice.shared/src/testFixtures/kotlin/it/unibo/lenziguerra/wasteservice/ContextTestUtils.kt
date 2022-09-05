package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.QakContext
import kotlinx.coroutines.CoroutineScope
import unibo.comm22.utils.CommUtils
import java.io.FileWriter
import kotlin.concurrent.thread
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

object ContextTestUtils {
    /**
     * @param contextsDescription Prolog contexts description, such as the content of <systemName>.pl
     */
    @JvmStatic
    fun createContextsFromString(hostName: String, scope: CoroutineScope, contextsDescription: String,
                                 rulesFilePath: String,
                                 contextName: String? = null
    ) {
        val tempFile = createTempFile("ctxdesc", ".pl")
        FileWriter(tempFile.toAbsolutePath().toString()).use {
            it.write(contextsDescription)
        }
        QakContext.createContexts(hostName, scope, tempFile.toAbsolutePath().toString(), rulesFilePath, contextName)
        thread {
            // Wait for createContexts to use it
            CommUtils.delay(1000)
            tempFile.deleteIfExists()
        }
    }
}