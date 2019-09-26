package com.dicio.dicio_android.components.fallback;

import com.dicio.component.output.views.BaseView;
import com.dicio.component.output.views.Header;

import java.util.ArrayList;
import java.util.List;

public class TextFallbackComponent implements FallbackComponent {
    // useless, just in case getInput() is used.
    private List<String> input = new ArrayList<>();

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
            add(new Header("I don't undersand, sorry :-("));
        }};
    }

    @Override
    public String getSpeechOutput() {
        return "I don't understand, sorry";
    }
}
