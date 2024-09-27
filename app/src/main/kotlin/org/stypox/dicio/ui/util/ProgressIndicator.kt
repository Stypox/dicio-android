package org.stypox.dicio.ui.util

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


data class Progress(
    val currentCount: Int,
    val totalCount: Int,
    val currentBytes: Long,
    val totalBytes: Long,
) {
    companion object {
        val UNKNOWN = Progress(0, 0, 0, 0)
    }
}

/**
 * @return a formatted string with the current and total bytes, and with the current and total count
 */
fun loadingProgressString(context: Context, progress: Progress): String {
    return loadingProgressString(context, progress.currentBytes, progress.totalBytes) +
            if (progress.totalCount > 1) {
                // do not show zero-based current count, unless the last item has finished and there
                // is no next item
                val increment = progress.currentCount != progress.totalCount ||
                        progress.currentBytes > 0 || progress.totalBytes > 0
                " (${progress.currentCount + if (increment) 1 else 0}/${progress.totalCount})"
            } else if (progress.currentCount > 1) {
                // do not show zero-based current count
                " (${progress.currentCount + 1})"
            } else {
                ""
            }
}

/**
 * @return a formatted string with the current and total bytes, or only the current if [totalBytes]
 * is `<= 0`, or an empty string if both are `<= 0`.
 */
fun loadingProgressString(context: Context, currentBytes: Long, totalBytes: Long): String {
    return if (totalBytes > 0) {
        Formatter.formatFileSize(context, currentBytes) + " / " +
                Formatter.formatFileSize(context, totalBytes)
    } else if (currentBytes > 0) {
        Formatter.formatFileSize(context, currentBytes)
    } else {
        ""
    }
}

/**
 * A small circular progress indicator that is indefinite if there is no information on the total
 * amount of items or of bytes being downloaded, and is in a circle percentage based on the number
 * of items (and further subdivided by the number of bytes) otherwise.
 */
@Composable
fun LoadingProgress(
    progress: Progress,
    color: Color = LocalContentColor.current,
) {
    if (progress.totalCount > 1 || progress.currentCount > 0) {
        val totalCount = if (progress.totalCount != 0) {
            progress.totalCount
        } else if (progress.totalBytes > 0 || progress.currentBytes > 0) {
            progress.currentCount + 1 // there is (at least) another item being processed
        } else {
            progress.currentCount
        }

        val secondaryProgress = if (progress.totalBytes > 0) {
            progress.currentBytes / progress.totalBytes.toFloat()
        } else {
            0.0f // there is no info available about the current item
        }
        val combinedProgress = (progress.currentCount + secondaryProgress) / totalCount

        SmallCircularProgressIndicator(combinedProgress, color)

    } else {
        LoadingProgress(progress.currentBytes, progress.totalBytes, color)
    }
}

/**
 * A small circular progress indicator that is indefinite if [totalBytes] is `<= 0`, and is in
 * a circle percentage of [currentBytes] `/` [totalBytes] otherwise.
 */
@Composable
fun LoadingProgress(
    currentBytes: Long,
    totalBytes: Long,
    color: Color = LocalContentColor.current,
) {
    if (totalBytes > 0) {
        SmallCircularProgressIndicator(currentBytes / totalBytes.toFloat(), color)
    } else {
        SmallCircularProgressIndicator(color)
    }
}



/**
 * A smaller circular progress indicator to use e.g. inside buttons.
 */
@Composable
fun SmallCircularProgressIndicator(
    progress: Float,
    color: Color = LocalContentColor.current,
) {
    CircularProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .progressSemantics()
            .size(24.dp),
        strokeWidth = 2.dp,
        color = color,
    )
}

@Suppress("detekt:style:MagicNumber")
@Preview
@Composable
fun SmallCircularProgressIndicatorProgressPreview() {
    Row {
        SmallCircularProgressIndicator(0.1f)
        SmallCircularProgressIndicator(0.5f)
        SmallCircularProgressIndicator(0.7f)
        SmallCircularProgressIndicator(1.0f)
    }
}

/**
 * A smaller circular progress indicator to use e.g. inside buttons.
 */
@Preview
@Composable
fun SmallCircularProgressIndicator(color: Color = LocalContentColor.current) {
    CircularProgressIndicator(
        modifier = Modifier
            .progressSemantics()
            .size(24.dp),
        strokeWidth = 2.dp,
        color = color,
    )
}
