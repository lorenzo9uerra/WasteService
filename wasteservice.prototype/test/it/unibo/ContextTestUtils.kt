package it.unibo

import it.unibo.kactor.QakContext
import kotlinx.coroutines.CoroutineScope
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

object ContextTestUtils {
    /**
     * @param contextsDescription Prolog contexts description, such as the content of <systemName>.pl
     */
    @JvmStatic
    fun createContextsFromString(hostName: String, scope: CoroutineScope, contextsDescription: String, rulesFilePath: String) {
        val tempFile = createTempFile("ctxdesc", ".pl")
        tempFile.writeText(contextsDescription)
        QakContext.createContexts(hostName, scope, tempFile.toAbsolutePath().toString(), rulesFilePath)
        tempFile.deleteIfExists()
    }
}