package it.unibo.lenziguerra.wasteservice.utils

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import unibo.actor22comm.utils.ColorsOut


private val localHostName = "localhost"
private val port = 8090
private val url = "http://$localHostName:$port/api/move"
private val httpclient: CloseableHttpClient = HttpClients.createDefault()


fun requestSynch(crilCmd: String?): Boolean {
    var endmove = false
    try {
        val entity = StringEntity(crilCmd)
        val httppost: org.apache.http.client.methods.HttpUriRequest = RequestBuilder.post()
            .setUri(url)
            .setHeader("Content-Type", "application/json")
            .setHeader("Accept", "application/json")
            .setEntity(entity)
            .build()
        val response: CloseableHttpResponse = httpclient.execute(httppost)
        //ColorsOut.out( "ClientUsingPost | sendCmd response= " + response );
        val jsonStr: String = EntityUtils.toString(response.entity)
        val jsonEndmove = JSONObject(jsonStr)
        ColorsOut.out("requestSynch | jsonEndmove=$jsonEndmove")
        if (jsonEndmove.get("endmove") != null) {
            endmove = jsonEndmove.getBoolean("endmove")
        }
    } catch (e: Exception) {
        ColorsOut.outerr("requestSynch | ERROR:" + e.message)
    }
    return endmove
}