package org.dicio.dicio_android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * See https://stackoverflow.com/a/54325629/9481500
 */
public final class PermissionRequesterActivity extends AppCompatActivity {
    private static final String TAG = "PermissionRequesterAct";

    private static final String PERMISSIONS_KEY = "PERMISSIONS_KEY";
    private static final String ACTION_PERMISSIONS_GRANTED
            = "GetPermissionsActivity.ACTION_PERMISSIONS_GRANTED";
    private static final String ACTION_PERMISSIONS_DENIED
            = "GetPermissionsActivity.ACTION_PERMISSIONS_DENIED";

    private final int permissionRequestCode = (int)(Math.random() * 100000);

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(
                this,
                getIntent().getStringArrayExtra(PERMISSIONS_KEY),
                permissionRequestCode
        );
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (requestCode == permissionRequestCode) {
            boolean allGranted = true;
            for (final int grantResult : grantResults) {
                allGranted = allGranted && grantResult == PackageManager.PERMISSION_GRANTED;
            }

            if (grantResults.length != 0 && allGranted) {
                sendBroadcast(new Intent(ACTION_PERMISSIONS_GRANTED));
            } else {
                sendBroadcast(new Intent(ACTION_PERMISSIONS_DENIED));
            }
            finish();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public interface OnPermissionResultListener {
        void onPermissionResult(boolean granted);
    }

    public static void requestPermissions(final Context context,
                                          final String[] permissions,
                                          final OnPermissionResultListener listener) {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PERMISSIONS_GRANTED);
        intentFilter.addAction(ACTION_PERMISSIONS_DENIED);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (ACTION_PERMISSIONS_GRANTED.equals(intent.getAction())) {
                    listener.onPermissionResult(true);
                } else if (ACTION_PERMISSIONS_DENIED.equals(intent.getAction())) {
                    listener.onPermissionResult(false);
                } else {
                    Log.w(TAG, "Unexpected intent received: " + intent);
                    listener.onPermissionResult(false); // just to be sure
                }
                context.unregisterReceiver(this);
            }
        }, intentFilter);

        final Intent intent = new Intent(context, PermissionRequesterActivity.class);
        intent.putExtra(PERMISSIONS_KEY, permissions);
        context.startActivity(intent);
    }
}
