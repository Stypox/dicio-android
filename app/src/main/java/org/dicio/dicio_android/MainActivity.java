package org.dicio.dicio_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.dicio.dicio_android.eval.SkillEvaluator;
import org.dicio.dicio_android.eval.SkillRanker;
import org.dicio.dicio_android.input.InputDevice;
import org.dicio.dicio_android.input.SpeechInputDevice;
import org.dicio.dicio_android.input.ToolbarInputDevice;
import org.dicio.dicio_android.input.VoskInputDevice;
import org.dicio.dicio_android.output.graphical.MainScreenGraphicalDevice;
import org.dicio.dicio_android.output.speech.AndroidTtsSpeechDevice;
import org.dicio.dicio_android.output.speech.NothingSpeechDevice;
import org.dicio.dicio_android.output.speech.SnackbarSpeechDevice;
import org.dicio.dicio_android.output.speech.ToastSpeechDevice;
import org.dicio.dicio_android.settings.SettingsActivity;
import org.dicio.dicio_android.skills.SkillHandler;
import org.dicio.dicio_android.util.BaseActivity;
import org.dicio.skill.output.SpeechOutputDevice;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int MICROPHONE_PERMISSION_REQUEST_CODE = 13893;

    private SharedPreferences preferences;

    private DrawerLayout drawer;
    private MenuItem textInputItem = null;

    @Nullable private SkillEvaluator skillEvaluator = null;
    private String currentInputDevicePreference = null;
    private boolean appJustOpened = false;
    private boolean textInputItemFocusJustChanged = false;

    ////////////////////////
    // Activity lifecycle //
    ////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        final ScrollView scrollView = findViewById(R.id.outputScrollView);
        scrollView.addOnLayoutChangeListener((v, left, top, right, bottom,
                                              oldLeft, oldTop, oldRight, oldBottom) -> {
            if (textInputItemFocusJustChanged && (oldBottom != bottom || oldTop != top)) {
                textInputItemFocusJustChanged = false; // the keyboard was opened because of menu
                scrollView.postDelayed(() ->
                        scrollView.scrollBy(0, oldBottom - bottom + top - oldTop), 10);
            }
        });

        currentInputDevicePreference = getInputDevicePreference();
        initializeSkillEvaluator();
        setupVoiceButton();
        appJustOpened = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (skillEvaluator != null) {
            if (skillEvaluator.getPrimaryInputDevice() instanceof ToolbarInputDevice) {
                ((ToolbarInputDevice) skillEvaluator.getPrimaryInputDevice())
                        .setTextInputItem(null);
            } else if (skillEvaluator.getSecondaryInputDevice() != null) {
                skillEvaluator.getSecondaryInputDevice().setTextInputItem(null);
            }
            skillEvaluator.cleanup();
        }
    }

    private void setupVoiceButton() {
        final ExtendedFloatingActionButton voiceFab = findViewById(R.id.voiceFab);
        final ProgressBar voiceLoading = findViewById(R.id.voiceLoading);
        if (skillEvaluator != null
                && skillEvaluator.getPrimaryInputDevice() instanceof SpeechInputDevice) {
            voiceFab.setVisibility(View.VISIBLE);
            voiceLoading.setVisibility(View.VISIBLE);
            ((SpeechInputDevice) skillEvaluator.getPrimaryInputDevice())
                    .setVoiceViews(voiceFab, voiceLoading);
        } else {
            voiceFab.setVisibility(View.GONE);
            voiceLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        final ToolbarInputDevice toolbarInputDevice;
        if (skillEvaluator == null) {
            toolbarInputDevice = null;
        } else if (skillEvaluator.getPrimaryInputDevice() instanceof ToolbarInputDevice) {
            toolbarInputDevice = (ToolbarInputDevice) skillEvaluator.getPrimaryInputDevice();
        } else {
            toolbarInputDevice = skillEvaluator.getSecondaryInputDevice();
        }

        if (toolbarInputDevice != null) {
            textInputItem = menu.findItem(R.id.action_text_input);
            textInputItem.setVisible(true);

            toolbarInputDevice.setTextInputItem(textInputItem);

            textInputItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(final MenuItem item) {
                    hideAllItems(menu);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(final MenuItem item) {
                    // resets the whole menu, setting `item`'s visibility to true
                    invalidateOptionsMenu();
                    return true;
                }
            });

            final SearchView textInputView = (SearchView) textInputItem.getActionView();
            textInputView.setQueryHint(getResources().getString(R.string.text_input_hint));
            textInputView.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            textInputView.setOnQueryTextFocusChangeListener(
                    (v, hasFocus) -> textInputItemFocusJustChanged = true);

        } else {
            // this should be unreachable, but just to be future-proof
            textInputItem.setVisible(false);
        }

        if (appJustOpened) {
            // now everything should have been initialized
            if (!(skillEvaluator.getPrimaryInputDevice() instanceof SpeechInputDevice)
                    || ActivityCompat.checkSelfPermission(this, RECORD_AUDIO)
                            == PERMISSION_GRANTED) {
                // if no voice permission start listening in onActivityResult
                skillEvaluator.getPrimaryInputDevice().tryToGetInput();
            }
            appJustOpened = false;
        }
        return true;
    }

    private void hideAllItems(Menu menu) {
        for (int i = 0; i < menu.size(); ++i) {
            final MenuItem item = menu.getItem(i);
            item.setVisible(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean reinitializeSkillEvaluator = false;

        final String inputDevicePreference = getInputDevicePreference();
        if (!inputDevicePreference.equals(currentInputDevicePreference)) {
            currentInputDevicePreference = inputDevicePreference;
            reinitializeSkillEvaluator = true;
        }

        if (reinitializeSkillEvaluator) {
            initializeSkillEvaluator();
            invalidateOptionsMenu();
            setupVoiceButton();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            final DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (textInputItem != null && textInputItem.isActionViewExpanded()) {
            invalidateOptionsMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MICROPHONE_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PERMISSION_GRANTED
                && skillEvaluator != null
                && skillEvaluator.getPrimaryInputDevice() instanceof SpeechInputDevice) {
            skillEvaluator.getPrimaryInputDevice().load();
            skillEvaluator.getPrimaryInputDevice().tryToGetInput();
        }
    }

    /////////////////////
    // Skill functions //
    /////////////////////

    private void initializeSkillEvaluator() {
        if (skillEvaluator != null) {
            skillEvaluator.cleanup();
        }

        final InputDevice primaryInputDevice = buildPrimaryInputDevice();

        final ToolbarInputDevice secondaryInputDevice;
        if (primaryInputDevice instanceof ToolbarInputDevice) {
            primaryInputDevice.load();
            secondaryInputDevice = null;
        } else {
            secondaryInputDevice = new ToolbarInputDevice();
            secondaryInputDevice.load();

            if (primaryInputDevice instanceof SpeechInputDevice
                    && ActivityCompat.checkSelfPermission(this, RECORD_AUDIO)
                            != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO},
                        MICROPHONE_PERMISSION_REQUEST_CODE);
            } else {
                primaryInputDevice.load(); // load only if permission granted
            }
        }

        final SpeechOutputDevice speechOutputDevice = buildSpeechOutputDevice();

        skillEvaluator = new SkillEvaluator(
                new SkillRanker( // Sections language is initialized in BaseActivity.setLocale
                        SkillHandler.getStandardSkillBatch(this, Sections.getCurrentLocale()),
                        SkillHandler.getFallbackSkill(this, Sections.getCurrentLocale())),
                primaryInputDevice,
                secondaryInputDevice,
                speechOutputDevice,
                new MainScreenGraphicalDevice(findViewById(R.id.outputViews)),
                this);
    }

    @NonNull
    private String getInputDevicePreference() {
        return preferences.getString(getString(R.string.pref_key_input_method), "");
    }

    private InputDevice buildPrimaryInputDevice() {
        if (currentInputDevicePreference.equals(
                getString(R.string.pref_val_input_method_text))) {
            return new ToolbarInputDevice();
        } else { // default
            return new VoskInputDevice(this);
        }
    }

    private SpeechOutputDevice buildSpeechOutputDevice() {
        final String preference = preferences
                .getString(getString(R.string.pref_key_speech_output_method), "");
        if (preference.equals(getString(R.string.pref_val_speech_output_method_nothing))) {
            return new NothingSpeechDevice();
        } else if (preference.equals(getString(R.string.pref_val_speech_output_method_snackbar))) {
            return new SnackbarSpeechDevice(findViewById(android.R.id.content));
        } else if (preference.equals(getString(R.string.pref_val_speech_output_method_toast))) {
            return new ToastSpeechDevice(this);
        } else { // default
            return new AndroidTtsSpeechDevice(this, Sections.getCurrentLocale());
        }
    }
}
