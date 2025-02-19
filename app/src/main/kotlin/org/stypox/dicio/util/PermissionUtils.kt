package org.stypox.dicio.util

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import org.dicio.skill.skill.SkillInfo

object PermissionUtils {
    private val TAG = PermissionUtils::class.java.simpleName

    /**
     * @param context the Android context
     * @param permissions an array of permissions to check (can be empty)
     * @return true if ALL of the provided permissions are granted when checking with [ActivityCompat.checkSelfPermission], false otherwise. So returns
     * true if the provided permissions array is empty.
     */
    fun checkPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    /**
     * @param grantResults an array of permission grant results to check (can be empty)
     * @return true if ALL of the provided grant results are equal to
     * [PackageManager.PERMISSION_GRANTED], false otherwise. So returns true if the provided grant
     * results array is empty.
     */
    fun areAllPermissionsGranted(vararg grantResults: Int): Boolean {
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * @param pm the Android package manager
     * @param permission the permission id for which to obtain the name
     * @return the localized label corresponding to the provided permission, or the provided
     * permission itself if something went wrong querying the package manager
     */
    fun getLocalizedLabel(pm: PackageManager, permission: String): CharSequence {
        return try {
            val permissionInfo = pm.getPermissionInfo(permission, 0)
            permissionInfo.loadLabel(pm)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Couldn't get permission group info", e)
            permission
        }
    }

    /**
     * @param context the Android context
     * @param skillInfo the skill info for which to create the permissions string
     * @return comma-separated list of the localized labels of all of the permissions the provided
     * skill info requires
     */
    fun getCommaJoinedPermissions(context: Context, skillInfo: SkillInfo): String {
        val pm = context.packageManager
        val commaJoinedPermissions = StringBuilder()
        for (permission in skillInfo.neededPermissions) {
            if (commaJoinedPermissions.isNotEmpty()) {
                commaJoinedPermissions.append(", ")
            }
            commaJoinedPermissions.append(getLocalizedLabel(pm, permission))
        }
        return commaJoinedPermissions.toString()
    }

    /**
     * @param skillInfo the skill info for which to create the secure settings string
     * @return comma-separated list of the secure settings the provided
     * skill info requires
     */
    fun getCommaJoinedSecureSettings(skillInfo: SkillInfo): String {
        return skillInfo.neededSecureSettings.joinToString(", ")
    }
}
