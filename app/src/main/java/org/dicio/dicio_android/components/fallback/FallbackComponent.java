package org.dicio.dicio_android.components.fallback;

import org.dicio.component.InputRecognizer;
import org.dicio.dicio_android.components.AssistanceComponent;

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
