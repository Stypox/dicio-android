package com.dicio.dicio_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.dicio.component.AssistanceComponent;
import com.dicio.dicio_android.components.TestComponent;
import com.dicio.dicio_android.components.WeatherComponent;
import com.dicio.dicio_android.components.fallback.TextFallbackComponent;
import com.dicio.dicio_android.eval.ComponentEvaluator;
import com.dicio.dicio_android.eval.ComponentRanker;
import com.dicio.dicio_android.io.graphical.MainScreenGraphicalDevice;
import com.dicio.dicio_android.io.input.ToolbarInputDevice;
import com.dicio.dicio_android.io.speech.ToastSpeechDevice;
import com.dicio.dicio_android.settings.SettingsActivity;
import com.dicio.dicio_android.util.ThemedActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ThemedActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    ToolbarInputDevice toolbarInputDevice;
    ComponentEvaluator componentEvaluator;
    MenuItem textInputItem;


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
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

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

        textInputItem = menu.findItem(R.id.action_text_input);
        toolbarInputDevice.setTextInputItem(textInputItem); // the textInput item might have changed

        textInputItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                item.setVisible(false);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /////////////////////////////////////
    // Assistance components functions //
    /////////////////////////////////////

    public void initializeComponentEvaluator() {
        List<AssistanceComponent> standardComponentBatch = new ArrayList<AssistanceComponent>() {{
            add(new TestComponent());
            add(new WeatherComponent());
        }};

        toolbarInputDevice = new ToolbarInputDevice();
        componentEvaluator = new ComponentEvaluator(
                new ComponentRanker(standardComponentBatch, new TextFallbackComponent()),
                toolbarInputDevice,
                new ToastSpeechDevice(this),
                new MainScreenGraphicalDevice(findViewById(R.id.outputViews), findViewById(R.id.outputScrollView)),
                this);
    }
}
