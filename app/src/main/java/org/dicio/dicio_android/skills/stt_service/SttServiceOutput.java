package org.dicio.dicio_android.skills.stt_service;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;

import org.dicio.dicio_android.MainActivity;
import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.chain.OutputGenerator;

import java.util.ArrayList;

public class SttServiceOutput extends OutputGenerator<SttServiceOutput.Data> {

    public static class Data {
        public String text;
    }

    @Override
    public void generate(final Data data) {

        final View sttServiceView = GraphicalOutputUtils.inflate(ctx().android(),
                R.layout.skill_stt_service);
        sttServiceView.findViewById(R.id.button_submit).setOnClickListener(view -> {
            //get results from recognizer and prepare for reporting
            final ArrayList<String> foundTexts = new ArrayList<>();
            foundTexts.add(data.text);
            final ArrayList<Float> confidenceScore = new ArrayList<>();
            //Because there is currently just one result, it gets 1.0 TODO check how to get more
            // results + confidence from vosk. When extending number of results,
            // implement EXTRA_MAX_RESULTS
            confidenceScore.add(1.0f);
            //Prepare Result Intent with Extras
            final MainActivity mainActivity = ((MainActivity) ctx().android());
            final Intent result = new Intent();
            result.putExtra(RecognizerIntent.EXTRA_RESULTS, foundTexts);
            result.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, confidenceScore);
            //This is for some apps, who use the Android SearchManager (e.g. ebay)
            //(in my eyes probably wrong implemented by them, however without it's not working...)
            result.putExtra(SearchManager.QUERY, data.text);
            mainActivity.setSttResult(Activity.RESULT_OK, result);
            mainActivity.onBackPressed();
        });
        ctx().getGraphicalOutputDevice().display(sttServiceView);
    }

    @Override
    public void cleanup() {

    }
}
