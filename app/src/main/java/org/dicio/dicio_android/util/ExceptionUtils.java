package org.dicio.dicio_android.util;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketException;

public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static boolean isNetworkError(final Throwable throwable) {
        return hasAssignableCause(throwable,
                // network api cancellation
                IOException.class, SocketException.class,
                // blocking code disposed
                InterruptedException.class, InterruptedIOException.class);
    }

    /**
     * @implNote taken from NewPipe, file util/ExceptionUtils.kt, created by @mauriciocolli
     */
    public static boolean hasAssignableCause(final Throwable throwable,
                                             final Class<?>... causesToCheck) {
        if (throwable == null) {
            return false;
        }

        // Check if throwable is a subtype of any of the causes to check
        for (final Class<?> causeClass : causesToCheck) {
            if (causeClass.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }

        @Nullable final Throwable currentCause = throwable.getCause();
        // Check if cause is not pointing to the same instance, to avoid infinite loops.
        if (throwable != currentCause) {
            return hasAssignableCause(currentCause, causesToCheck);
        } else {
            return false;
        }
    }

    /**
     * Returns the string representation of the stack trace
     * @param throwable the exception
     * @return the stack trace of {@param throwable} as a string
     */
    public static String getStackTraceString(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
