package org.dicio.dicio_android.skills.open;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.Nullable;

import org.dicio.skill.standard.StandardResult;
import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.OutputGenerator;
import org.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import org.dicio.dicio_android.output.speech.SpeechOutputDevice;
import org.dicio.dicio_android.util.StringUtils;

import java.util.List;

import static org.dicio.dicio_android.Sentences_en.open;

public class OpenOutput implements OutputGenerator<StandardResult> {

    @Override
    public void generate(final StandardResult data,
                         final Context context,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {

        final String userAppName = data.getCapturingGroup(open.what).trim();
        final PackageManager packageManager = context.getPackageManager();
        final ApplicationInfo applicationInfo = getMostSimilarApp(packageManager, userAppName);

        if (applicationInfo == null) {
            speechOutputDevice.speak(
                    context.getString(R.string.skill_open_unknown_app, userAppName));

        } else {
            speechOutputDevice.speak(context.getString(R.string.skill_open_opening,
                    packageManager.getApplicationLabel(applicationInfo)));

            final Intent launchIntent =
                    packageManager.getLaunchIntentForPackage(applicationInfo.packageName);
            launchIntent.setAction(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
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

                final int currentDistance = StringUtils.levenshteinDistance(appName,
                        packageManager.getApplicationLabel(currentApplicationInfo).toString());
                if (currentDistance < bestDistance) {
                    bestDistance = currentDistance;
                    bestApplicationInfo = currentApplicationInfo;
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        if (bestDistance > 5) {
            return null;
        }
        return bestApplicationInfo;
    }
}
