package com.dicio.dicio_android.components;

import com.dicio.component.AssistanceComponent;
import com.dicio.component.input.standard.StandardRecognizer;
import com.dicio.component.output.OutputGenerator;
import com.dicio.component.output.views.BaseView;
import com.dicio.component.output.views.DescribedImage;
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
            add(new Image("https://cdn.pixabay.com/photo/2014/06/03/19/38/board-361516_960_720.jpg", Image.SourceType.url));
            add(new Image("https://cdn.pixabay.com/photo/2017/06/10/07/18/list-2389219_960_720.png", Image.SourceType.url));
        }};
    }

    @Override
    public String getSpeechOutput() {
        return "This is a test!";
    }

    @Override
    public Optional<OutputGenerator> nextOutputGenerator() {
        return Optional.of(new OutputGenerator() {
            @Override
            public List<BaseView> getGraphicalOutput() {
                final DescribedImage clickableDescribedImage = new DescribedImage(
                        "https://cdn.pixabay.com/photo/2016/08/26/15/54/checklist-1622517_960_720.png",
                        Image.SourceType.url,
                        "This is a described image!",
                        "<b>Description lorem ipsum <a href=\"https://example.org\">dolor sit amet</a></b> consectetur adipisci elit",
                        true);

                return new ArrayList<BaseView>() {{
                    add(new Description("<a href=\"https://example.org\">This link brings you to https://example.org</a>", true));
                    add(clickableDescribedImage);
                    add(new DescribedImage(
                            "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Ophrys_apifera_Bienen-Ragwurz_2014.jpg/1200px-Ophrys_apifera_Bienen-Ragwurz_2014.jpg",
                            Image.SourceType.url, "Header",
                            "<b>Description without html enabled</b>",
                            false));
                }};
            }

            @Override
            public String getSpeechOutput() {
                return "This test is great, isn't it? XD";
            }
        });
    }

    @Override
    public Optional<List<AssistanceComponent>> nextAssistanceComponents() {
        return Optional.empty();
    }
}
