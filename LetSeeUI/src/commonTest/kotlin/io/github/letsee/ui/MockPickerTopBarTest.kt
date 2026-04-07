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
                    fullUrl = TestFixtures.LongFullUrl,
                    onBack = {},
                )
            }
        }

        onNodeWithTag("mock_picker_path_label", useUnmergedTree = true).fetchSemanticsNode()
        onNodeWithText("[BaseURL]/arrangement-manager", substring = true).fetchSemanticsNode()
        onNodeWithTag("mock_picker_back", useUnmergedTree = true).fetchSemanticsNode()
        onNodeWithTag("mock_picker_copy", useUnmergedTree = true).fetchSemanticsNode()
    }

    @Test
    fun mockPickerTopBar_backButton_invokesCallback() = runComposeUiTest {
        var backPressed = false

        setContent {
            LetSeeTheme(darkTheme = false) {
                MockPickerTopBar(
                    title = TestFixtures.LongFullUrl,
                    fullUrl = TestFixtures.LongFullUrl,
                    onBack = { backPressed = true },
                )
            }
        }

        onNodeWithTag("mock_picker_back", useUnmergedTree = true).performClick()
        assertTrue(backPressed)
    }
}
