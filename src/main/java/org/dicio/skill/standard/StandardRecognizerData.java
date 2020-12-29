package org.dicio.skill.standard;

import org.dicio.skill.chain.InputRecognizer;

public class StandardRecognizerData {
    private final InputRecognizer.Specificity specificity;
    private final Sentence[] sentences;

    public StandardRecognizerData(InputRecognizer.Specificity specificity, Sentence... sentences) {
        this.specificity = specificity;
        this.sentences = sentences;
    }

    public InputRecognizer.Specificity getSpecificity() {
        return specificity;
    }

    public Sentence[] getSentences() {
        return sentences;
    }
}
