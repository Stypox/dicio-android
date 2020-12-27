package org.dicio.skill.standard;

import org.junit.Test;

import static org.junit.Assert.*;

public class PartialScoreResultTest {
    private static final float floatEqualsDelta = 0.0001f;

    @Test
    public void testDropAt0point75() {
        assertEquals(0.0f, PartialScoreResult.dropAt0point75(0.0f), floatEqualsDelta);
        assertEquals(1.0f, PartialScoreResult.dropAt0point75(1.0f), floatEqualsDelta);

        assertTrue(PartialScoreResult.dropAt0point75(0.8f) > 0.8f);
        assertTrue(PartialScoreResult.dropAt0point75(0.7f) < 0.65f);
        assertTrue(PartialScoreResult.dropAt0point75(0.6f) < 0.4f);
    }

    @Test
    public void testDropAt0point6() {
        assertEquals(0.0f, PartialScoreResult.dropAt0point6(0.0f), floatEqualsDelta);
        assertEquals(1.0f, PartialScoreResult.dropAt0point6(1.0f), floatEqualsDelta);

        assertTrue(PartialScoreResult.dropAt0point6(0.7f) > 0.8f);
        assertTrue(PartialScoreResult.dropAt0point6(0.6f) < 0.7f);
        assertTrue(PartialScoreResult.dropAt0point6(0.5f) < 0.4f);
    }

    @Test
    public void testScore() {
        assertEquals(0.0f, new PartialScoreResult(1000000, 1000000).value(5), floatEqualsDelta);
        assertEquals(1.0f, new PartialScoreResult(      0,       0).value(5), floatEqualsDelta);
    }
}