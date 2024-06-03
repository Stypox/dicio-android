package org.stypox.dicio.skills.open

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard2.StandardRecognizerData
import org.dicio.skill.standard2.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Open
import org.stypox.dicio.util.StringUtils

class OpenSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Open>)
    : StandardRecognizerSkill<Open>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, scoreResult: Open): SkillOutput {
        val userAppName = when (scoreResult) {
            is Open.Query -> scoreResult.what?.trim { it <= ' ' }
        }
        val packageManager: PackageManager = ctx.android.packageManager
        val applicationInfo = userAppName?.let { getMostSimilarApp(packageManager, it) }

        if (applicationInfo != null) {
            val launchIntent: Intent =
                packageManager.getLaunchIntentForPackage(applicationInfo.packageName)!!
            launchIntent.action = Intent.ACTION_MAIN
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ctx.android.startActivity(launchIntent)
        }

        return OpenOutput(
            appName = applicationInfo?.loadLabel(packageManager)?.toString() ?: userAppName,
            packageName = applicationInfo?.packageName,
        )
    }

    companion object {
        private fun getMostSimilarApp(
            packageManager: PackageManager,
            appName: String
        ): ApplicationInfo? {
            val resolveInfosIntent = Intent(Intent.ACTION_MAIN, null)
            resolveInfosIntent.addCategory(Intent.CATEGORY_LAUNCHER)

            @SuppressLint("QueryPermissionsNeeded") // we need to query all apps
            val resolveInfos: List<ResolveInfo> =
                packageManager.queryIntentActivities(resolveInfosIntent, 0)
            var bestDistance = Int.MAX_VALUE
            var bestApplicationInfo: ApplicationInfo? = null

            for (resolveInfo in resolveInfos) {
                try {
                    val currentApplicationInfo: ApplicationInfo = packageManager.getApplicationInfo(
                        resolveInfo.activityInfo.packageName, PackageManager.GET_META_DATA
                    )
                    val currentDistance = StringUtils.customStringDistance(
                        appName,
                        packageManager.getApplicationLabel(currentApplicationInfo).toString()
                    )
                    if (currentDistance < bestDistance) {
                        bestDistance = currentDistance
                        bestApplicationInfo = currentApplicationInfo
                    }
                } catch (ignored: PackageManager.NameNotFoundException) {
                }
            }
            return if (bestDistance > 5) null else bestApplicationInfo
        }
    }
}
