package org.dicio.dicio_android.skills.open;

import static org.dicio.dicio_android.Sentences_en.open;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.Nullable;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.standard.StandardResult;

import java.util.List;

public class OpenOutput extends OutputGenerator<StandardResult> {

    @Override
    public void generate(final StandardResult data) {

        final String userAppName = data.getCapturingGroup(open.what).trim();
        final PackageManager packageManager = ctx().android().getPackageManager();
        final ApplicationInfo applicationInfo = getMostSimilarApp(packageManager, userAppName);

        if (applicationInfo == null) {
            ctx().getSpeechOutputDevice().speak(ctx().android().getString(
                    R.string.skill_open_unknown_app, userAppName));

        } else {
            ctx().getSpeechOutputDevice().speak(ctx().android().getString(
                    R.string.skill_open_opening,
                    packageManager.getApplicationLabel(applicationInfo)));

            final Intent launchIntent =
                    packageManager.getLaunchIntentForPackage(applicationInfo.packageName);
            launchIntent.setAction(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx().android().startActivity(launchIntent);
        }
    }

    @Nullable
    private static ApplicationInfo getMostSimilarApp(final PackageManager packageManager,
                                                     final String appName) {
        final Intent resolveInfosIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveInfosIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        @SuppressLint("QueryPermissionsNeeded") // we need to query all apps
        final List<ResolveInfo> resolveInfos =
                packageManager.queryIntentActivities(resolveInfosIntent, 0);

        int bestDistance = Integer.MAX_VALUE;
        ApplicationInfo bestApplicationInfo = null;
        for (final ResolveInfo resolveInfo : resolveInfos) {
            try {
                final ApplicationInfo currentApplicationInfo = packageManager.getApplicationInfo(
                        resolveInfo.activityInfo.packageName, PackageManager.GET_META_DATA);

                final int currentDistance = StringUtils.customStringDistance(appName,
                        packageManager.getApplicationLabel(currentApplicationInfo).toString());
                if (currentDistance < bestDistance) {
                    bestDistance = currentDistance;
                    bestApplicationInfo = currentApplicationInfo;
                }
            } catch (final PackageManager.NameNotFoundException ignored) {
            }
        }

        if (bestDistance > 5) {
            return null;
        }
        return bestApplicationInfo;
    }

    @Override
    public void cleanup() {
    }
}
