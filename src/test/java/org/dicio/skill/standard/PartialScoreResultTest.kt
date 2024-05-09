package org.dicio.skill.standard

import org.dicio.skill.standard.PartialScoreResult.Companion.dropAt0point6
import org.dicio.skill.standard.PartialScoreResult.Companion.dropAt0point75
import org.junit.Assert
import org.junit.Test

class PartialScoreResultTest {
    @Test
    fun testDropAt0point75() {
        Assert.assertEquals(0.0f, dropAt0point75(0.0f), FLOAT_EQUALS_DELTA)
        Assert.assertEquals(1.0f, dropAt0point75(1.0f), FLOAT_EQUALS_DELTA)

        Assert.assertTrue(dropAt0point75(0.8f) > 0.8f)
        Assert.assertTrue(dropAt0point75(0.7f) < 0.65f)
        Assert.assertTrue(dropAt0point75(0.6f) < 0.4f)
    }

    @Test
    fun testDropAt0point6() {
        Assert.assertEquals(0.0f, dropAt0point6(0.0f), FLOAT_EQUALS_DELTA)
        Assert.assertEquals(1.0f, dropAt0point6(1.0f), FLOAT_EQUALS_DELTA)

        Assert.assertTrue(dropAt0point6(0.7f) > 0.8f)
        Assert.assertTrue(dropAt0point6(0.6f) < 0.7f)
        Assert.assertTrue(dropAt0point6(0.5f) < 0.4f)
    }

    @Test
    fun testScore() {
        Assert.assertEquals(0.0f, PartialScoreResult(1000000, 1000000).value(5), FLOAT_EQUALS_DELTA)
        Assert.assertEquals(1.0f, PartialScoreResult(0, 0).value(5), FLOAT_EQUALS_DELTA)
    }

    companion object {
        private const val FLOAT_EQUALS_DELTA = 0.0001f
    }
}
