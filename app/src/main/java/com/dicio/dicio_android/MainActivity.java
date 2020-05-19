package com.dicio.dicio_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.dicio.component.standard.StandardRecognizer;
import com.dicio.dicio_android.components.AssistanceComponent;
import com.dicio.dicio_android.components.ChainAssistanceComponent;
import com.dicio.dicio_android.components.fallback.TextFallbackComponent;
import com.dicio.dicio_android.components.output.LyricsOutput;
import com.dicio.dicio_android.components.output.SearchOutput;
import com.dicio.dicio_android.components.output.WeatherOutput;
import com.dicio.dicio_android.components.processing.GeniusProcessor;
import com.dicio.dicio_android.components.processing.OpenWeatherMapProcessor;
import com.dicio.dicio_android.components.processing.QwantProcessor;
import com.dicio.dicio_android.eval.ComponentEvaluator;
import com.dicio.dicio_android.eval.ComponentRanker;
import com.dicio.dicio_android.input.AzureSpeechInputDevice;
import com.dicio.dicio_android.input.InputDevice;
import com.dicio.dicio_android.input.SpeechInputDevice;
import com.dicio.dicio_android.input.ToolbarInputDevice;
import com.dicio.dicio_android.output.graphical.MainScreenGraphicalDevice;
import com.dicio.dicio_android.output.speech.ToastSpeechDevice;
import com.dicio.dicio_android.settings.SettingsActivity;
import com.dicio.dicio_android.util.ThemedActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ThemedActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private InputDevice inputDevice;
    private ComponentEvaluator componentEvaluator;
    @NonNull private String currentInputDevicePreference;

    ////////////////////////
    // Activity lifecycle //
    ////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        currentInputDevicePreference = getInputDevicePreference();
        initializeComponentEvaluator();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem textInputItem = menu.findItem(R.id.action_text_input);
        if (inputDevice instanceof ToolbarInputDevice) {
            textInputItem.setVisible(true);
            ((ToolbarInputDevice) inputDevice).setTextInputItem(textInputItem);

            textInputItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    hideAllItems(menu);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // resets the whole menu, setting `item`'s visibility to true
                    invalidateOptionsMenu();
                    return true;
                }
            });

            SearchView textInputView = (SearchView) textInputItem.getActionView();
            textInputView.setQueryHint(getResources().getString(R.string.text_input_hint));
            textInputView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        } else {
            textInputItem.setVisible(false);
        }

        MenuItem voiceInputItem = menu.findItem(R.id.action_voice_input);
        if (inputDevice instanceof SpeechInputDevice) {
            voiceInputItem.setVisible(true);
            ((SpeechInputDevice) inputDevice).setVoiceInputItem(voiceInputItem,
                    getResources().getDrawable(R.drawable.ic_mic_white),
                    getResources().getDrawable(R.drawable.ic_mic_none_white)); // TODO set theme-compliant drawables
        } else {
            voiceInputItem.setVisible(false);
        }

        return true;
    }

    private void hideAllItems(Menu menu) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            item.setVisible(false);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        String inputDevicePreference = getInputDevicePreference();
        if (!inputDevicePreference.equals(currentInputDevicePreference)) {
            currentInputDevicePreference = inputDevicePreference;
            initializeComponentEvaluator();
            invalidateOptionsMenu();
        }
    }

    @NonNull
    String getInputDevicePreference() {
        String inputDevicePreference = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.settings_key_input_method), null);

        if (inputDevicePreference == null) {
            return getString(R.string.settings_value_input_method_text);
        } else {
            return inputDevicePreference;
        }
    }

    /////////////////////////////////////
    // Assistance components functions //
    /////////////////////////////////////

    public void initializeComponentEvaluator() {
        List<AssistanceComponent> standardComponentBatch = new ArrayList<AssistanceComponent>() {{
            add(new ChainAssistanceComponent.Builder()
                    .recognize(new StandardRecognizer(Sentences.weather))
                    .process(new OpenWeatherMapProcessor())
                    .output(new WeatherOutput()));
            add(new ChainAssistanceComponent.Builder()
                    .recognize(new StandardRecognizer(Sentences.search))
                    .process(new QwantProcessor())
                    .output(new SearchOutput()));
            add(new ChainAssistanceComponent.Builder()
                    .recognize(new StandardRecognizer(Sentences.lyrics))
                    .process(new GeniusProcessor())
                    .output(new LyricsOutput()));
        }};

        if (currentInputDevicePreference.equals(getString(R.string.settings_value_input_method_azure))) {
            inputDevice = new AzureSpeechInputDevice(this);
        } else /*if (currentInputDevicePreference.equals(getString(R.string.settings_value_input_method_text)))*/ {
            inputDevice = new ToolbarInputDevice();
        }

        componentEvaluator = new ComponentEvaluator(
                new ComponentRanker(standardComponentBatch, new TextFallbackComponent()),
                inputDevice,
                new ToastSpeechDevice(this),
                new MainScreenGraphicalDevice(findViewById(R.id.outputViews), findViewById(R.id.outputScrollView)),
                this);
    }
}
