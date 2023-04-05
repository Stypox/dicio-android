package org.stypox.dicio.util

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Scanner

object ConnectionUtils {
    @Throws(IOException::class)
    fun getPage(
        url: String?,
        headers: Map<String?, String?>
    ): String {
        val connection = URL(url).openConnection()
        for ((key, value) in headers) {
            connection.setRequestProperty(key, value)
        }
        val scanner = Scanner(connection.getInputStream())
        return scanner.useDelimiter("\\A").next()
    }

    @Throws(IOException::class)
    fun getPage(url: String?): String {
        return getPage(url, emptyMap<String?, String>())
    }

    @Throws(IOException::class, JSONException::class)
    fun getPageJson(url: String?): JSONObject {
        return JSONObject(getPage(url))
    }

    @Throws(UnsupportedEncodingException::class)
    fun urlEncode(s: String?): String {
        return URLEncoder.encode(s, "utf8")
    }

    @Throws(UnsupportedEncodingException::class)
    fun urlDecode(s: String?): String {
        return URLDecoder.decode(s, "utf8")
    }
}