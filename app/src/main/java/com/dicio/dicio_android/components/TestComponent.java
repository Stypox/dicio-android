package com.dicio.dicio_android.components;

import com.dicio.component.AssistanceComponent;
import com.dicio.component.input.standard.StandardRecognizer;
import com.dicio.component.output.OutputGenerator;
import com.dicio.component.output.views.BaseView;
import com.dicio.component.output.views.Description;
import com.dicio.component.output.views.Header;
import com.dicio.component.output.views.Image;
import com.dicio.dicio_android.Sentences;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestComponent extends StandardRecognizer implements AssistanceComponent {
    private List<String> words = new ArrayList<>();

    public TestComponent() {
        super(Sentences.debug);
    }

    @Override
    public void calculateOutput() throws Throwable {

    }

    @Override
    public List<BaseView> getGraphicalOutput() {
        return new ArrayList<BaseView>() {{
            add(new Header("Test!"));
            add(new Description("<h1>For debugging purposes :-)</h1>", true));
            add(new Image("https://i.stack.imgur.com/6BNcp.png", Image.SourceType.url));
            add(new Image("http://dakotalapse.com/wp-content/uploads/2015/04/Sequence-07.Still002.jpg", Image.SourceType.url));
        }};
    }

    @Override
    public String getSpeechOutput() {
        return "This is a test!";
    }

    @Override
    public Optional<OutputGenerator> nextOutputGenerator() {
        return Optional.empty();
    }

    @Override
    public Optional<List<AssistanceComponent>> nextAssistanceComponents() {
        return Optional.empty();
    }
}
