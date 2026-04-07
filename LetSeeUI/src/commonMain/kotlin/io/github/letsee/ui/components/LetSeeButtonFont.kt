package io.github.letsee.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import letsee_kmm.letseeui.generated.resources.Res
import letsee_kmm.letseeui.generated.resources.poppins_medium
import org.jetbrains.compose.resources.Font

@Composable
fun letSeeButtonFontFamily(): FontFamily {
    val medium = Font(
        resource = Res.font.poppins_medium,
        weight = FontWeight.Medium,
    )
    return remember(medium) { FontFamily(medium) }
}

fun TextStyle.withLetSeeFontFamily(fontFamily: FontFamily): TextStyle =
    copy(fontFamily = fontFamily)
