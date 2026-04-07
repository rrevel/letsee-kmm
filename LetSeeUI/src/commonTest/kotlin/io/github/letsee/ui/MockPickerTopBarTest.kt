package io.github.letsee.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.letsee.ui.testutils.TestFixtures
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class MockPickerTopBarTest {

    @Test
    fun mockPickerTopBar_showsLongRequestPath_inTitle() = runComposeUiTest {
        setContent {
            LetSeeTheme(darkTheme = false) {
                MockPickerTopBar(
                    title = TestFixtures.LongBaseUrlPath,
                    onBack = {},
                )
            }
        }

        onNodeWithText("[BaseURL]/arrangement-manager", substring = true).fetchSemanticsNode()
        onNodeWithTag("mock_picker_back", useUnmergedTree = true).fetchSemanticsNode()
    }

    @Test
    fun mockPickerTopBar_backButton_invokesCallback() = runComposeUiTest {
        var backPressed = false

        setContent {
            LetSeeTheme(darkTheme = false) {
                MockPickerTopBar(
                    title = TestFixtures.LongFullUrl,
                    onBack = { backPressed = true },
                )
            }
        }

        onNodeWithTag("mock_picker_back", useUnmergedTree = true).performClick()
        assertTrue(backPressed)
    }
}
