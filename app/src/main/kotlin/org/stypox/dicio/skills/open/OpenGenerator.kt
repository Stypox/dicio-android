package org.stypox.dicio.skills.open

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.standard.StandardResult
import org.stypox.dicio.Sentences_en.open
import org.stypox.dicio.util.StringUtils

class OpenGenerator : OutputGenerator<StandardResult>() {
    override fun generate(data: StandardResult): SkillOutput {
        val userAppName = data.getCapturingGroup(open.what)?.trim { it <= ' ' }
        val packageManager: PackageManager = ctx().android!!.packageManager
        val applicationInfo = userAppName?.let { getMostSimilarApp(packageManager, it) }

        if (applicationInfo != null) {
            val launchIntent: Intent =
                packageManager.getLaunchIntentForPackage(applicationInfo.packageName)!!
            launchIntent.action = Intent.ACTION_MAIN
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ctx().android!!.startActivity(launchIntent)
        }

        return OpenOutput(
            context = ctx().android!!,
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
