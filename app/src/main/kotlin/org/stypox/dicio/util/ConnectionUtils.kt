package org.stypox.dicio.util

import android.net.Uri
import org.json.JSONException
import org.json.JSONObject
import org.stypox.dicio.util.ConnectionUtils.percentEncode
import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Scanner

object ConnectionUtils {
    @Throws(IOException::class)
    fun getPage(
        url: String,
        headers: Map<String, String?>
    ): String {
        val connection = URL(url).openConnection()
        for ((key, value) in headers) {
            connection.setRequestProperty(key, value)
        }
        val scanner = Scanner(connection.getInputStream())
        return scanner.useDelimiter("\\A").next()
    }

    @Throws(IOException::class)
    fun getPage(url: String): String {
        return getPage(url, emptyMap())
    }

    @Throws(IOException::class, JSONException::class)
    fun getPageJson(url: String): JSONObject {
        return JSONObject(getPage(url))
    }

    /**
     * Encodes [s] as `application/x-www-form-urlencoded` so that it can be used as a parameter in
     * URL query strings. Note: space will be encoded as `+` which makes sense for URL query strings
     * but not for the URL path, in that case use [percentEncode] instead.
     */
    fun urlEncode(s: String): String {
        return URLEncoder.encode(s, "utf8")
    }

    /**
     * Decodes [s] from `application/x-www-form-urlencoded`.
     */
    fun urlDecode(s: String): String {
        return URLDecoder.decode(s, "utf8")
    }

    /**
     * Percent-encodes [s] so that it can be used in a URL path. Note: space will be encoded as
     * `%20`.
     */
    fun percentEncode(s: String): String {
        return Uri.encode(s)
    }

    /**
     * Percent-decodes [s].
     */
    fun percentDecode(s: String): String {
        return Uri.decode(s)
    }
}