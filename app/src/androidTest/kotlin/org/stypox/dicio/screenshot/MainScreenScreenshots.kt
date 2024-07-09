package org.stypox.dicio.screenshot

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.stypox.dicio.MainActivity

@RunWith(AndroidJUnit4::class)
class ScreenshotTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun makeScreenshot() {
        rule.takeScreenshot("en-US", "0")
        rule.takeScreenshot("en-US", "1")
    }
}
