package org.dicio.dicio_android.util;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.Context;

import androidx.core.app.ActivityCompat;

import org.dicio.skill.Skill;

public class PermissionUtils {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static boolean checkPermissions(final Context context, final String... permissions) {
        for (final String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean areAllPermissionsGranted(final int... grantResults) {
        for (final int grantResult : grantResults) {
            if (grantResult != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static String[] permissionsArrayFromSkill(final Skill skill) {
        if (skill.getSkillInfo() == null) {
            return EMPTY_STRING_ARRAY;
        }
        return skill.getSkillInfo().getNeededPermissions().toArray(EMPTY_STRING_ARRAY);
    }
}
