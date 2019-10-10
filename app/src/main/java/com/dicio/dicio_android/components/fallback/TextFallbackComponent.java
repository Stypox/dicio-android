package com.dicio.dicio_android.components.fallback;

import android.content.Context;

import com.dicio.component.output.views.BaseView;
import com.dicio.component.output.views.Header;
import com.dicio.dicio_android.R;

import java.util.ArrayList;
import java.util.List;

public class TextFallbackComponent implements FallbackComponent {
    // useless, just in case getInput() is used.
    private List<String> input = new ArrayList<>();
    private Context context;

    public TextFallbackComponent(Context context) {
        this.context = context;
    }

    @Override
    public void setInput(List<String> words) {
        this.input = words;
    }

    @Override
    public List<String> getInput() {
        return input;
    }

    @Override
    public List<BaseView> getGraphicalOutput() {
        return new ArrayList<BaseView>() {{
            add(new Header(context.getString(R.string.eval_header_no_match)));
        }};
    }

    @Override
    public String getSpeechOutput() {
        return context.getString(R.string.eval_speech_no_match);
    }
}
