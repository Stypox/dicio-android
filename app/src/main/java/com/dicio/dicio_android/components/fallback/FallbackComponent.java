package com.dicio.dicio_android.components.fallback;

import com.dicio.component.AssistanceComponent;

public interface FallbackComponent extends AssistanceComponent {
    @Override
    default Specificity specificity() {
        return Specificity.low; // useless
    }

    @Override
    default float score() {
        return 0; // useless
    }
}
