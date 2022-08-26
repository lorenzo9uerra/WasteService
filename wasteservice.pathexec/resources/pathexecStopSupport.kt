/*
 -------------------------------------------------------------------------------------------------
 */

import org.json.JSONObject
import java.io.File


object pathexecStopSupport {
    fun readStepTime(   ) : String{
        val config = File("stepTimeConfig.json").readText(Charsets.UTF_8)
        val jsonObject   = JSONObject( config )
        return jsonObject.getString("step")
    }
}