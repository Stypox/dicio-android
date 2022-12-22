package org.stypox.dicio.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import org.dicio.skill.Skill;
import org.dicio.skill.SkillInfo;

public final class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private PermissionUtils() {
    }

    /**
     * @param context the Android context
     * @param permissions an array of permissions to check (can be empty)
     * @return true if ALL of the provided permissions are granted when checking with {@link
     *         ActivityCompat#checkSelfPermission(Context, String)}, false otherwise. So returns
     *         true if the provided permissions array is empty.
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
     * @param grantResults an array of permission grant results to check (can be empty)
     * @return true if ALL of the provided grant results are equal to {@link
     *         PackageManager#PERMISSION_GRANTED}, false otherwise. So returns true if the provided
     *         grant results array is empty.
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
     * @param skillInfo the skill info from which to obtain the permissions it needs (can be null)
     * @return an array of the permissions the provided skill info requires. If the provided skill
     *         info is null, an empty array is returned.
     */
    public static String[] permissionsArrayFromSkillInfo(@Nullable final SkillInfo skillInfo) {
        if (skillInfo == null) {
            return EMPTY_STRING_ARRAY;
        }
        return skillInfo.getNeededPermissions().toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Calls {@link #permissionsArrayFromSkillInfo(SkillInfo)} on {@link Skill#getSkillInfo()}
     * @param skill the skill from which to obtain the permissions it needs
     * @return an array of the permissions the provided skill requires. If the provided skill has
     *         {@link Skill#getSkillInfo()} equal to null, an empty array is returned.
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

    /**
     * @param context the Android context
     * @param skillInfo the skill info for which to create the permissions string
     * @return comma-separated list of the localized labels of all of the permissions the provided
     *         skill info requires
     */
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
