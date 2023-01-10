package org.stypox.dicio.skills.telephone;

import org.dicio.numbers.util.Number;
import org.dicio.skill.Skill;
import org.dicio.skill.chain.InputRecognizer;

import java.util.List;

public class ContactChooserIndex extends Skill {
    private final List<NameNumberPair> contacts;
    private String input;
    private int index;

    ContactChooserIndex(final List<NameNumberPair> contacts) {
        this.contacts = contacts;
    }

    @Override
    public InputRecognizer.Specificity specificity() {
        return InputRecognizer.Specificity.high;
    }

    @Override
    public void setInput(final String theInput,
                         final List<String> inputWords,
                         final List<String> normalizedWordKeys) {
        this.input = theInput;
    }

    @Override
    public float score() {
        index = ctx().requireNumberParserFormatter()
                .extractNumbers(input)
                .preferOrdinal(true)
                .get().stream()
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .filter(Number::isInteger)
                .mapToInt(number -> (int) number.integerValue())
                .findFirst()
                .orElse(0);

        return index <= 0 || index > contacts.size() ? 0.0f : 1.0f;
    }

    @Override
    public void processInput() {
    }

    @Override
    public void generateOutput() {
        if (index > 0 && index <= contacts.size()) {
            final NameNumberPair contact = contacts.get(index - 1);
            ConfirmCallOutput.callAfterConfirmation(this, contact.name, contact.number);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        input = null;
    }
}
