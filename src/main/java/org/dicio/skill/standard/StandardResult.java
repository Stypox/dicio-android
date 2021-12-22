package org.dicio.skill.standard;

import org.dicio.skill.util.WordExtractor;

import java.util.Map;

public class StandardResult {
    private final String sentenceId;
    private final String input;
    private final Map<String, InputWordRange> capturingGroups;

    public StandardResult(final String sentenceId,
                   final String input,
                   final Map<String, InputWordRange> capturingGroups) {
        this.sentenceId = sentenceId;
        this.input = input;
        this.capturingGroups = capturingGroups;
    }

    public String getSentenceId() {
        return sentenceId;
    }

    public Map<String, InputWordRange> getCapturingGroupRanges() {
        return capturingGroups;
    }

    public String getCapturingGroup(final String name) {
        if (capturingGroups.containsKey(name)) {
            return WordExtractor.extractCapturingGroup(input, capturingGroups.get(name));
        } else {
            return null;
        }
    }
}
