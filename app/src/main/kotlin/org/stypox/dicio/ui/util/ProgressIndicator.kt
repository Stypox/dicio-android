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
