package org.stypox.dicio.skills.app_search

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.AppSearch
import org.stypox.dicio.util.StringUtils

class AppSearchSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<AppSearch>)
    : StandardRecognizerSkill<AppSearch>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: AppSearch): SkillOutput {
        val userAppName = when (inputData) {
            is AppSearch.Query -> inputData.app?.trim { it <= ' ' }
            else -> null
        }
        val userQuery = when (inputData) {
            is AppSearch.Query -> inputData.query?.trim { it <= ' ' }
            else -> null
        }
        
        val packageManager: PackageManager = ctx.android.packageManager
        val applicationInfo = userAppName?.let { getMostSimilarApp(packageManager, it) }
        var success = false

        if (applicationInfo != null && userQuery != null) {
            success = launchAppWithSearch(ctx, packageManager, applicationInfo, userQuery)
        }

        return AppSearchOutput(
            appName = applicationInfo?.loadLabel(packageManager)?.toString() ?: userAppName,
            packageName = applicationInfo?.packageName,
            searchQuery = userQuery,
            success = success
        )
    }

    private fun launchAppWithSearch(ctx: SkillContext, packageManager: PackageManager, applicationInfo: ApplicationInfo, query: String): Boolean {
        val packageName = applicationInfo.packageName
        try {
            // Handle different apps with their specific search intents
            when {
                // YouTube
                packageName.contains("youtube", ignoreCase = true) -> {
                    val intent = Intent(Intent.ACTION_SEARCH)
                    intent.setPackage(packageName)
                    intent.putExtra("query", query)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    ctx.android.startActivity(intent)
                    return true
                }
                // Spotify
                packageName.contains("spotify", ignoreCase = true) -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse("spotify:search:$query"))
                    intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://${ctx.android.packageName}"))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    ctx.android.startActivity(intent)
                    return true
                }
                // Generic search intent as fallback
                else -> {
                    val intent = Intent(Intent.ACTION_SEARCH)
                    intent.setPackage(packageName)
                    intent.putExtra("query", query)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    ctx.android.startActivity(intent)
                    return true
                }
            }
        } catch (e: Exception) {
            // If the specific intent fails, try a web search as fallback
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                when {
                    packageName.contains("youtube", ignoreCase = true) -> {
                        intent.data = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
                    }
                    packageName.contains("spotify", ignoreCase = true) -> {
                        intent.data = Uri.parse("https://open.spotify.com/search/${Uri.encode(query)}")
                    }
                    else -> {
                        return false
                    }
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ctx.android.startActivity(intent)
                return true
            } catch (e: Exception) {
                return false
            }
        }
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
