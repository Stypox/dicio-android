package com.dicio.dicio_android.util;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.dicio.dicio_android.R;

abstract public class ThemedActivity extends AppCompatActivity {
    private int preferenceToTheme(@Nullable String preference) {
        if (preference == null) {
            return R.style.LightAppTheme;
        }

        switch (preference) {
            case "dark":
                return R.style.DarkAppTheme;
            case "light": default:
                return R.style.LightAppTheme;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String preference = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("theme", "light");
        setTheme(preferenceToTheme(preference));
    }
}
