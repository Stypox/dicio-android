package org.dicio.dicio_android.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import org.dicio.skill.Skill;
import org.dicio.skill.SkillInfo;

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * @return true if ALL of the provided permissions are granted when checking with {@link
     *         ActivityCompat#checkSelfPermission(Context, String)}, false otherwise
     */
    public static boolean checkPermissions(final Context context, final String... permissions) {
        for (final String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if ALL of the provided grant results are equal to
     *         {@link PackageManager#PERMISSION_GRANTED}, false otherwise
     */
    public static boolean areAllPermissionsGranted(final int... grantResults) {
        for (final int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return an array of the permissions the provided skill info requires
     */
    public static String[] permissionsArrayFromSkillInfo(@Nullable final SkillInfo skillInfo) {
        if (skillInfo == null) {
            return EMPTY_STRING_ARRAY;
        }
        return skillInfo.getNeededPermissions().toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * @return an array of the permissions the provided skill requires
     */
    public static String[] permissionsArrayFromSkill(final Skill skill) {
        return permissionsArrayFromSkillInfo(skill.getSkillInfo());
    }

    /**
     * @param pm the Android package manager
     * @param permission the permission id for which to obtain the name
     * @return the localized label corresponding to the provided permission, or the provided
     *         permission itself if something went wrong querying the package manager
     */
    @NonNull
    public static CharSequence getLocalizedLabel(final PackageManager pm, final String permission) {
        try {
            final PermissionInfo permissionInfo = pm.getPermissionInfo(permission, 0);
            return permissionInfo.loadLabel(pm);
        } catch (final PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Couldn't get permission group info", e);
            return permission;
        }
    }

    public static String getCommaJoinedPermissions(final Context context,
                                                   final SkillInfo skillInfo) {
        final PackageManager pm = context.getPackageManager();
        final StringBuilder commaJoinedPermissions = new StringBuilder();
        for (final String permission : skillInfo.getNeededPermissions()) {
            if (commaJoinedPermissions.length() != 0) {
                commaJoinedPermissions.append(", ");
            }
            commaJoinedPermissions.append(getLocalizedLabel(pm, permission));
        }
        return commaJoinedPermissions.toString();
    }
}
