package org.stypox.dicio.error

import java.io.InterruptedIOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object ExceptionUtils {
    fun isNetworkError(throwable: Throwable?): Boolean {
        return hasAssignableCause(
            throwable,

            UnknownHostException::class.java,
            SSLException::class.java,
            ConnectException::class.java,
            SocketException::class.java,

            // blocking code disposed
            InterruptedException::class.java,
            InterruptedIOException::class.java,
        )
    }

    /**
     * @implNote taken from NewPipe, file util/ExceptionUtils.kt, created by @mauriciocolli
     */
    fun hasAssignableCause(
        throwable: Throwable?,
        vararg causesToCheck: Class<*>
    ): Boolean {
        if (throwable == null) {
            return false
        }

        // Check if throwable is a subtype of any of the causes to check
        for (causeClass in causesToCheck) {
            if (causeClass.isAssignableFrom(throwable.javaClass)) {
                return true
            }
        }
        val currentCause = throwable.cause
        // Check if cause is not pointing to the same instance, to avoid infinite loops.
        return if (throwable !== currentCause) {
            hasAssignableCause(currentCause, *causesToCheck)
        } else {
            false
        }
    }

    /**
     * Returns the string representation of the stack trace
     * @param throwable the exception
     * @return the stack trace of {@param throwable} as a string
     */
    fun getStackTraceString(throwable: Throwable): String {
        val stringWriter = StringWriter()
        throwable.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()
    }
}