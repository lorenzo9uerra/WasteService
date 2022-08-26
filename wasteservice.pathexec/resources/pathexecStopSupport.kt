/*
 -------------------------------------------------------------------------------------------------
 */

import org.json.JSONObject
import unibo.comm22.utils.ColorsOut
import java.io.File


object pathexecStopSupport {
    fun readStepTime() : String {
        val config = File("stepTimeConfig.json").let {
            if (it.exists()) {
                it.readText(Charsets.UTF_8)
            } else {
                val cfg = """
                    |{
                    |   "step": "345"
                    |}
                """.trimMargin()
                it.writeText(cfg)
                ColorsOut.outappl("Created config file stepTimeConfig.json", ColorsOut.ANSI_PURPLE)
                cfg
            }
        }
        val jsonObject   = JSONObject( config )
        return jsonObject.getString("step")
    }
}