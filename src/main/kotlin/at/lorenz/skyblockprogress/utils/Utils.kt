package at.lorenz.skyblockprogress.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils

object Utils {

    private val builder: HttpClientBuilder =
        HttpClients.custom().setUserAgent("SkyHanni")
            .setDefaultHeaders(
                mutableListOf(
                    BasicHeader("Pragma", "no-cache"),
                    BasicHeader("Cache-Control", "no-cache")
                )
            )
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .build()
            )
            .useSystemProperties()


    fun getJSONResponse(urlString: String, silentError: Boolean = false): JsonObject {
        val client = builder.build()
        try {
            client.execute(HttpGet(urlString)).use { response ->
                val entity = response.entity
                if (entity != null) {
                    val retSrc = EntityUtils.toString(entity)
                    return JsonParser.parseString(retSrc) as JsonObject
                }
            }
        } catch (throwable: Throwable) {
            if (silentError) {
                throw throwable
            } else {
                throwable.printStackTrace()
            }
        } finally {
            client.close()
        }
        return JsonObject()
    }
}