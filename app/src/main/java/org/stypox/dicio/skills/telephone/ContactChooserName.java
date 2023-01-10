package org.stypox.dicio.skills.telephone;

import org.dicio.skill.Skill;
import org.dicio.skill.chain.InputRecognizer;
import org.stypox.dicio.util.StringUtils;

import java.util.Comparator;
import java.util.List;

public class ContactChooserName extends Skill {
    private final List<NameNumberPair> contacts;
    private String input;
    private NameNumberPair bestContact;

    ContactChooserName(final List<NameNumberPair> contacts) {
        this.contacts = contacts;
    }

    @Override
    public InputRecognizer.Specificity specificity() {
        // use a low specificity to prefer the index-based contact chooser
        return InputRecognizer.Specificity.low;
    }

    @Override
    public void setInput(final String theInput,
                         final List<String> inputWords,
                         final List<String> normalizedWordKeys) {
        this.input = theInput;
    }

    @Override
    public float score() {
        class Pair {
            final NameNumberPair nameNumberPair;
            final int distance;

            Pair(final NameNumberPair nameNumberPair, final int distance) {
                this.nameNumberPair = nameNumberPair;
                this.distance = distance;
            }
        }

        input = input.trim();
        bestContact = contacts.stream()
                .map(nameNumberPair -> new Pair(nameNumberPair,
                        StringUtils.customStringDistance(input, nameNumberPair.name)))
                .filter(pair -> pair.distance < 6)
                .min(Comparator.comparingInt(a -> a.distance))
                .map(pair -> pair.nameNumberPair)
                .orElse(null);

        return bestContact == null ? 0.0f : 1.0f;
    }

    @Override
    public void processInput() {
    }

    @Override
    public void generateOutput() {
        if (bestContact != null) {
            ConfirmCallOutput.callAfterConfirmation(this, bestContact.name, bestContact.number);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        bestContact = null;
    }
}
