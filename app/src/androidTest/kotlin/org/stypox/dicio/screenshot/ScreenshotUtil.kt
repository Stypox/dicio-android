package org.stypox.dicio.screenshot

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * Takes a screenshot of the current Compose root, and saves it on the host PC. The language must
 * be one
 */
fun ComposeContentTestRule.takeScreenshot(language: String, name: String) {
    onRoot()
        .captureToImage()
        .asAndroidBitmap()
        .saveToHostPc(language, name)
}

/**
 * Saves [this] `Bitmap` to the host PC using an untrusted HTTPS connection. The host PC's IP
 * address is always 10.0.2.2, and this function requires that `screenshot_server.py` is running on
 * the host PC.
 */
fun Bitmap.saveToHostPc(language: String, name: String) {
    val image = ByteArrayOutputStream()
        .use { out ->
            compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }

    unsafeOkHttpClient
        .newCall(
            Request.Builder()
                .url("https://10.0.2.2:5000/save_screenshot/$language/$name")
                .put(image.toRequestBody())
                .build()
        )
        .execute()
}

/**
 * Android disallows establishing plain HTTP connections even in test mode. Therefore we use an
 * HTTPS connection with the screenshot server, except that the screenshot server itself does not
 * have a trusted certificate (obviously), so we need to use an instance of OkHttpClient that trusts
 * all certificates.
 *
 * The returned client will work only for 10.0.2.2, i.e. the IP that the Android emulator assigns to
 * the host PC.
 *
 * Code taken from <a href="https://stackoverflow.com/a/25992879">
 *     https://stackoverflow.com/a/25992879</a>.
 */
val unsafeOkHttpClient by lazy {
    try {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

        val builder = OkHttpClient.Builder()
        builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        builder.hostnameVerifier { hostname, _ -> hostname == "10.0.2.2" }

        return@lazy builder.build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}
