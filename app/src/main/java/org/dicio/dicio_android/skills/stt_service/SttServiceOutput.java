package org.dicio.dicio_android.skills.stt_service;

import android.app.Activity;
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
    public void generate(Data data) {

        final View sttServiceView = GraphicalOutputUtils.inflate(ctx().android(),
                R.layout.skill_stt_service);
        sttServiceView.findViewById(R.id.button_submit).setOnClickListener(view -> {
            ArrayList<String> foundTexts = new ArrayList<>();
            foundTexts.add(data.text);

            MainActivity mainActivity = ((MainActivity)ctx().android());
            Intent result = new Intent();
            result.putExtra(RecognizerIntent.EXTRA_RESULTS, foundTexts);
            mainActivity.setSstResult(Activity.RESULT_OK, result);
            mainActivity.onBackPressed();
        });
        ctx().getGraphicalOutputDevice().display(sttServiceView);
    }

    @Override
    public void cleanup() {

    }
}
