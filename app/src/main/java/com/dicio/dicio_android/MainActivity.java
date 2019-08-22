package com.dicio.dicio_android;

import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.dicio.component.AssistanceComponent;
import com.dicio.component.input.InputRecognizer;
import com.dicio.component.output.OutputGenerator;
import com.dicio.component.output.views.BaseView;
import com.dicio.component.output.views.DescribedImage;
import com.dicio.component.output.views.Description;
import com.dicio.component.output.views.Header;
import com.dicio.component.output.views.Image;
import com.dicio.dicio_android.renderer.OutputRenderer;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    LinearLayout outputViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        outputViews = findViewById(R.id.outputViews);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        try {
            outputViews.addView(OutputRenderer.renderComponentOutput(new AssistanceComponent() {
                @Override
                public InputRecognizer.Specificity specificity() {
                    return null;
                }

                @Override
                public void setInput(List<String> words) {

                }

                @Override
                public List<String> getInput() {
                    return null;
                }

                @Override
                public float score() {
                    return 0;
                }

                @Override
                public void calculateOutput() {

                }

                @Override
                public List<BaseView> getGraphicalOutput() {
                    final Image clickableImage = new Image("https://i.stack.imgur.com/M5XAy.png", Image.SourceType.url) {{
                        setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(String imageSource, SourceType sourceType) {
                                Toast.makeText(getApplicationContext(), imageSource, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }};

                    final DescribedImage clickableDescribedImage = new DescribedImage("https://i.stack.imgur.com/M5XAy.png", Image.SourceType.url,
                            "Header", "<b>Description bellissima ieeeeee <a href=\"https://example.org\">link text</a></b> ciao ciao come va ciao ciao come va") {{
                        setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(String imageSource, Image.SourceType imageSourceType, String headerText, String descriptionText) {
                                Toast.makeText(getApplicationContext(), headerText, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }};

                    return new ArrayList<BaseView>() {{
                        add(new Header("Hello!"));
                        add(new Description("<h1>Hello!</h1>"));
                        add(clickableImage);
                        add(new Image("https://i.stack.imgur.com/6BNcp.png", Image.SourceType.url));
                        add(new Image("http://dakotalapse.com/wp-content/uploads/2015/04/Sequence-07.Still002.jpg",Image.SourceType.url));
                        add(new Description("<a href=\"https://example.org\">link text</a>"));
                        add(clickableDescribedImage);
                        add(new DescribedImage("https://i.stack.imgur.com/6BNcp.png", Image.SourceType.url, "Header", "<b>Description bellissima ieeeeee</b>"));
                    }};
                }

                @Override
                public String getSpeechOutput() {
                    return null;
                }

                @Override
                public Optional<OutputGenerator> nextOutputGenerator() {
                    return Optional.empty();
                }

                @Override
                public Optional<List<AssistanceComponent>> nextAssistanceComponents() {
                    return Optional.empty();
                }
            }, this));
            outputViews.addView(OutputRenderer.renderComponentOutput(new AssistanceComponent() {
                @Override
                public InputRecognizer.Specificity specificity() {
                    return null;
                }

                @Override
                public void setInput(List<String> words) {

                }

                @Override
                public List<String> getInput() {
                    return null;
                }

                @Override
                public float score() {
                    return 0;
                }

                @Override
                public void calculateOutput() {

                }

                @Override
                public List<BaseView> getGraphicalOutput() {
                    final Image clickableImage = new Image("https://i.stack.imgur.com/M5XAy.png", Image.SourceType.url) {{
                        setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(String imageSource, SourceType sourceType) {
                                Toast.makeText(getApplicationContext(), imageSource, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }};

                    final DescribedImage clickableDescribedImage = new DescribedImage("https://i.stack.imgur.com/M5XAy.png", Image.SourceType.url,
                            "Header", "<b>Description bellissima ieeeeee <a href=\"https://example.org\">link text</a></b> ciao ciao come va ciao ciao come va") {{
                        setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(String imageSource, Image.SourceType imageSourceType, String headerText, String descriptionText) {
                                Toast.makeText(getApplicationContext(), headerText, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }};

                    return new ArrayList<BaseView>() {{
                        add(new Header("Hello!"));
                        add(new Description("<h1>Hello!</h1>"));
                        add(clickableImage);
                        add(new Image("https://i.stack.imgur.com/6BNcp.png", Image.SourceType.url));
                        add(new Image("http://dakotalapse.com/wp-content/uploads/2015/04/Sequence-07.Still002.jpg",Image.SourceType.url));
                        add(new Description("<a href=\"https://example.org\">link text</a>"));
                        add(clickableDescribedImage);
                        add(new DescribedImage("https://i.stack.imgur.com/6BNcp.png", Image.SourceType.url, "Header", "<b>Description bellissima ieeeeee</b>"));
                    }};
                }

                @Override
                public String getSpeechOutput() {
                    return null;
                }

                @Override
                public Optional<OutputGenerator> nextOutputGenerator() {
                    return Optional.empty();
                }

                @Override
                public Optional<List<AssistanceComponent>> nextAssistanceComponents() {
                    return Optional.empty();
                }
            }, this));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
