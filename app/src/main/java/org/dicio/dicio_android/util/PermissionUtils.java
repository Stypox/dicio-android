package org.dicio.dicio_android.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import org.dicio.dicio_android.R;
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
     * @return an array of the permissions the provided skill requires
     */
    public static String[] permissionsArrayFromSkill(final Skill skill) {
        if (skill.getSkillInfo() == null) {
            return EMPTY_STRING_ARRAY;
        }
        return skill.getSkillInfo().getNeededPermissions().toArray(EMPTY_STRING_ARRAY);
    }

    public static class PermissionData {
        private final PackageManager pm;
        private final String permissionName;
        @Nullable private final PermissionInfo permissionInfo;

        public PermissionData(final PackageManager pm,
                              final String permissionName,
                              @Nullable final PermissionInfo permissionInfo) {
            this.pm = pm;
            this.permissionName = permissionName;
            this.permissionInfo = permissionInfo;
        }

        final CharSequence getLocalizedLabel() {
            if (permissionInfo == null) {
                return permissionName; // fallback to group name
            } else {
                return permissionInfo.loadLabel(pm);
            }
        }

        @Nullable
        final Drawable getIcon() {
            if (permissionInfo == null) {
                return null; // fallback to no drawable
            } else {
                return permissionInfo.loadIcon(pm);
            }
        }
    }

    /**
     * @return the permission data for the provided permission obtained with the provided package
     *         manager, or a fallback permission data if some error occurred
     */
    public static PermissionData getPermissionData(final PackageManager pm,
                                                   final String permission) {
        try {
            return new PermissionData(pm, permission, pm.getPermissionInfo(permission, 0));
        } catch (final PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Couldn't get permission group info", e);
            return new PermissionData(pm, permission, null);
        }
    }

    public static String getMissingPermissionsMessage(final Context context,
                                                      final SkillInfo skillInfo) {
        final PackageManager pm = context.getPackageManager();
        final StringBuilder permissionList = new StringBuilder();
        for (final String permission : skillInfo.getNeededPermissions()) {
            if (permissionList.length() != 0) {
                permissionList.append(", ");
            }
            permissionList.append(getPermissionData(pm, permission).getLocalizedLabel());
        }

        return context.getString(R.string.eval_missing_permissions_named_skill,
                context.getString(skillInfo.getNameResource()), permissionList.toString());
    }
}
