package org.stypox.dicio.skills.notify

import android.util.Log // Import Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.util.getString
import org.stypox.dicio.skills.notify.NotifyHandler.Companion.TAG

data class NotifyOutput(val notifications: List<Notification>): SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String {
        Log.d(TAG, "getSpeechOutput called")
        var response = ""
        if (notifications.isNotEmpty()) {
            notifications.forEachIndexed { index, notification ->
                Log.d(TAG, "index=$index appName='${notification.appName}', message='${notification.message}'")
                val speechPart = ctx.getString(R.string.skill_notify_message, notification.appName, notification.message ?: "")
                Log.d(TAG, "Speech part for notification $index: '$speechPart'")
                response += speechPart
                if (notifications.size > 1 && index < notifications.size - 1) {
                    response += ". "
                }
            }
        }
        else {
            response = ctx.getString(R.string.no_notifications)
            Log.d(TAG, response)
        }
        Log.d(TAG, response)
        Log.e(TAG, "notifications: " + notifications.toString())
        return response.trim()
    }

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        if (notifications.isNotEmpty()) {
            Column {
                for (notification in notifications) {
                    Text(
                        text = notification.appName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = notification.message.toString(), // String? -> "null" if message is null
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}