package com.dicio.dicio_android.components.fallback;

import com.dicio.component.InputRecognizer;
import com.dicio.dicio_android.components.AssistanceComponent;

public interface FallbackComponent extends AssistanceComponent {
    @Override
    default InputRecognizer.Specificity specificity() {
        return InputRecognizer.Specificity.low; // useless
    }

    @Override
    default float score() {
        return 0; // useless
    }
}
