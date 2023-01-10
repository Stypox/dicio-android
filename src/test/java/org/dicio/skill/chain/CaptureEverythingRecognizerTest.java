package org.dicio.skill.chain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import java.util.Arrays;

public class CaptureEverythingRecognizerTest {
    @Test
    public void testSpecificityScore() {
        final CaptureEverythingRecognizer cer = new CaptureEverythingRecognizer();
        assertEquals(InputRecognizer.Specificity.low, cer.specificity());
        assertEquals(1.0f, cer.score(), 0.0f);
    }

    @Test
    public void testInput() {
        final String input = "Some inpùt";
        final CaptureEverythingRecognizer cer = new CaptureEverythingRecognizer();

        cer.setInput(input, Arrays.asList("Some", "inpùt"), Arrays.asList("Some", "input"));
        assertSame(input, cer.getResult().getCapturingGroup(null));
        assertSame(input, cer.getResult().getCapturingGroup("hello"));
        assertSame(input, cer.getResult().getCapturingGroup(input));
    }
}
