package org.stypox.dicio

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
import org.dicio.skill.output.SpeechOutputDevice
import org.stypox.dicio.eval.SkillEvaluator
import org.stypox.dicio.eval.SkillRanker
import org.stypox.dicio.io.input.InputDevice
import org.stypox.dicio.io.input.SpeechInputDevice
import org.stypox.dicio.io.input.ToolbarInputDevice
import org.stypox.dicio.io.input.VoskInputDevice
import org.stypox.dicio.io.input.stt_service.SttServiceActivity
import org.stypox.dicio.io.graphical.GraphicalOutputDevice
import org.stypox.dicio.io.graphical.MainScreenGraphicalDevice
import org.stypox.dicio.io.speech.AndroidTtsSpeechDevice
import org.stypox.dicio.io.speech.NothingSpeechDevice
import org.stypox.dicio.io.speech.SnackbarSpeechDevice
import org.stypox.dicio.io.speech.ToastSpeechDevice
import org.stypox.dicio.settings.SettingsActivity
import org.stypox.dicio.skills.SkillHandler
import org.stypox.dicio.util.BaseActivity
import org.stypox.dicio.util.PermissionUtils

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var preferences: SharedPreferences
    private lateinit var drawer: DrawerLayout
    private lateinit var textInputItem: MenuItem
    private var skillEvaluator: SkillEvaluator? = null
    private var appJustOpened = false
    private var resumingFromSettings = false
    private var textInputItemFocusJustChanged = false

    ////////////////////////
    // Activity lifecycle //
    ////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        requestInitialPermissionsIfNeeded()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        drawer = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        val scrollView = findViewById<ScrollView>(R.id.outputScrollView)
        scrollView.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            if (textInputItemFocusJustChanged && (oldBottom != bottom || oldTop != top)) {
                textInputItemFocusJustChanged = false // the keyboard was opened because of menu
                scrollView.postDelayed({
                    scrollView.scrollBy(
                        0,
                        oldBottom - bottom + top - oldTop
                    )
                }, 10)
            }
        }

        appJustOpened = true // determines whether to show initial panel and start listening
        initializeSkillEvaluator()
        setupVoiceButton()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        tryToGetInput()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroySkillEvaluator()
        SkillHandler.releaseSkillContext()
    }

    private fun requestInitialPermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PermissionUtils.checkPermissions(this, Manifest.permission.POST_NOTIFICATIONS)
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!PermissionUtils.checkPermissions(this, Manifest.permission.RECORD_AUDIO) &&
            preferences.getString(
                getString(R.string.pref_key_input_method),
                ""
            ) != getString(R.string.pref_val_input_method_text)
        ) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                NOTIFICATIONS_OR_MICROPHONE_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setupVoiceButton() {
        val voiceFab = findViewById<ExtendedFloatingActionButton>(R.id.voiceFab)
        val voiceLoading = findViewById<ProgressBar>(R.id.voiceLoading)
        if (skillEvaluator?.primaryInputDevice is SpeechInputDevice) {
            voiceFab.visibility = View.VISIBLE
            voiceLoading.visibility = View.VISIBLE
            (skillEvaluator?.primaryInputDevice as SpeechInputDevice)
                .setVoiceViews(voiceFab, voiceLoading)
        } else {
            voiceFab.visibility = View.GONE
            voiceLoading.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val toolbarInputDevice = (skillEvaluator?.primaryInputDevice as? ToolbarInputDevice)
            ?: skillEvaluator?.secondaryInputDevice
        if (toolbarInputDevice != null) {
            textInputItem = menu.findItem(R.id.action_text_input)
            textInputItem.isVisible = true
            toolbarInputDevice.setTextInputItem(textInputItem)
            textInputItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    hideAllItems(menu)
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    // resets the whole menu, setting `item`'s visibility to true
                    invalidateOptionsMenu()
                    return true
                }
            })

            val textInputView = textInputItem.actionView as SearchView
            textInputView.queryHint = resources.getString(R.string.text_input_hint)
            textInputView.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            textInputView.setOnQueryTextFocusChangeListener { _, _ ->
                textInputItemFocusJustChanged = true
            }

        } else {
            // this should be unreachable, but just to be future-proof
            textInputItem.isVisible = false
        }

        if (appJustOpened) {
            // now everything should have been initialized
            tryToGetInput()
            appJustOpened = false
        }
        return true
    }

    private fun tryToGetInput() {
        if (skillEvaluator?.primaryInputDevice !is SpeechInputDevice
            || PermissionUtils.checkPermissions(this, Manifest.permission.RECORD_AUDIO)
        ) {
            // if no voice permission start listening in onRequestPermissionsResult
            skillEvaluator?.primaryInputDevice?.tryToGetInput(false)
        }
    }

    private fun hideAllItems(menu: Menu) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            item.isVisible = false
        }
    }

    override fun onPause() {
        super.onPause()
        skillEvaluator?.cancelGettingInput()
    }

    override fun onResume() {
        super.onResume()

        // theme/language changes can cause recreation, so everything will be initialized there
        if (resumingFromSettings && !isRecreating) {
            // reinitialize everything if resuming from settings
            resumingFromSettings = false
            initializeSkillEvaluator()
            invalidateOptionsMenu()
            setupVoiceButton()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            drawer.closeDrawer(GravityCompat.START)
            resumingFromSettings = true
        } else if (item.itemId == R.id.action_stt_service) {
            startActivity(Intent(this, SttServiceActivity::class.java))
            drawer.closeDrawer(GravityCompat.START)
        }
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (textInputItem.isActionViewExpanded) {
            invalidateOptionsMenu()
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATIONS_OR_MICROPHONE_PERMISSION_REQUEST_CODE -> {
                permissions
                    .filterIndexed { index, _ ->
                        grantResults[index] == PackageManager.PERMISSION_GRANTED
                    }
                    .forEach { permission ->
                        when (permission) {
                            Manifest.permission.RECORD_AUDIO -> {
                                val speechInputDevice = skillEvaluator?.primaryInputDevice
                                        as? SpeechInputDevice ?: return
                                speechInputDevice.load()
                                speechInputDevice.tryToGetInput(false)
                            }

                            Manifest.permission.POST_NOTIFICATIONS -> {
                                (application as App).initNotificationChannels()
                            }
                        }
                    }
            }

            SKILL_PERMISSIONS_REQUEST_CODE -> {
                skillEvaluator?.onSkillRequestPermissionsResult(grantResults)
            }
        }
        // SETTINGS_PERMISSIONS_REQUEST_CODE results are ignored
    }

    /////////////////////
    // Skill functions //
    /////////////////////
    private fun initializeSkillEvaluator() {
        destroySkillEvaluator()
        val primaryInputDevice = buildPrimaryInputDevice()
        val secondaryInputDevice: ToolbarInputDevice?
        if (primaryInputDevice is ToolbarInputDevice) {
            primaryInputDevice.load()
            secondaryInputDevice = null
        } else {
            secondaryInputDevice = ToolbarInputDevice()
            secondaryInputDevice.load()
            if (primaryInputDevice !is SpeechInputDevice ||
                PermissionUtils.checkPermissions(this, Manifest.permission.RECORD_AUDIO)
            ) {
                // load only if permission granted (requesting the permission is handled above)
                primaryInputDevice.load()
            }
        }
        val speechOutputDevice = buildSpeechOutputDevice()
        val graphicalOutputDevice: GraphicalOutputDevice = MainScreenGraphicalDevice(
            findViewById(R.id.outputScrollView), findViewById(R.id.outputLayout)
        )
        SkillHandler.setSkillContextDevices(speechOutputDevice)
        skillEvaluator =
            SkillEvaluator( // Sections language is initialized in BaseActivity.setLocale
                SkillRanker(SkillHandler.standardSkillBatch, SkillHandler.fallbackSkill),
                primaryInputDevice,
                secondaryInputDevice,
                speechOutputDevice,
                graphicalOutputDevice,
                this
            )
        skillEvaluator?.showInitialPanel()
    }

    private fun buildPrimaryInputDevice(): InputDevice {
        val preference = preferences
            .getString(getString(R.string.pref_key_input_method), "")
        return if (preference == getString(R.string.pref_val_input_method_text)) {
            ToolbarInputDevice()
        } else { // default
            VoskInputDevice(this)
        }
    }

    private fun buildSpeechOutputDevice(): SpeechOutputDevice {
        val preference = preferences
            .getString(getString(R.string.pref_key_speech_output_method), "")
        return when (preference) {
            getString(R.string.pref_val_speech_output_method_nothing) -> {
                NothingSpeechDevice()
            }

            getString(R.string.pref_val_speech_output_method_snackbar) -> {
                SnackbarSpeechDevice(findViewById(android.R.id.content))
            }

            getString(R.string.pref_val_speech_output_method_toast) -> {
                ToastSpeechDevice(this)
            }

            else -> { // default
                AndroidTtsSpeechDevice(this, Sections.currentLocale)
            }
        }
    }

    private fun destroySkillEvaluator() {
        skillEvaluator?.cleanup()
        skillEvaluator = null
    }

    companion object {
        const val NOTIFICATIONS_OR_MICROPHONE_PERMISSION_REQUEST_CODE = 13893
        const val SKILL_PERMISSIONS_REQUEST_CODE = 1928430
        const val SETTINGS_PERMISSIONS_REQUEST_CODE = 420938
    }
}
