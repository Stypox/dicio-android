package org.dicio.dicio_android.util;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import org.dicio.dicio_android.R;

/**
 * A base for all of the activities that automatically recreates itself when the theme or the
 * language (locale) change
 */
public abstract class BaseActivity extends LocaleAwareActivity {

    private int currentTheme;
    private boolean isRecreating = false;

    /**
     * Override this if extending activity needs some specific light/dark themes (different than the
     * default {@code R.style.Light/DarkAppTheme}).
     *
     * @return the id of the style resource to use as theme based on preferences
     */
    @StyleRes
    protected int getThemeFromPreferences() {
        return ThemeUtils.chooseThemeBasedOnPreferences(this,
                R.style.LightAppTheme, R.style.DarkAppTheme);
    }


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        isRecreating = false;
        currentTheme = getThemeFromPreferences();
        setTheme(currentTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void recreate() {
        isRecreating = true;
        super.recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTheme != getThemeFromPreferences()) {
            recreate();
        }
    }

    protected boolean isRecreating() {
        return isRecreating;
    }
}
