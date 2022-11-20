package org.dicio.dicio_android.skills.stt_service;

import org.dicio.skill.chain.IntermediateProcessor;

public class SttServiceProcessor extends IntermediateProcessor<SimpleForwardRecognizer.Result, SttServiceOutput.Data> {
    @Override
    public SttServiceOutput.Data process(SimpleForwardRecognizer.Result data) throws Exception {
        final SttServiceOutput.Data result = new SttServiceOutput.Data();
        result.text = data.input;
        return result;
    }
}
