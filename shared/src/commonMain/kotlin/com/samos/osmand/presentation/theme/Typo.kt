package com.samos.osmand.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import osmand.shared.generated.resources.Res
import osmand.shared.generated.resources.roboto_medium
import osmand.shared.generated.resources.roboto_regular

// Default Material 3 typography values
val baseline = Typography()

/**
 *  Font weight:
 *      thin = 100
 *      extra light = 200
 *      light = 300
 *      regular = 400
 *      medium = 500
 *      semibold = 600
 *      bold = 700
 *      extra bold = 800
 *      black = 900
 */

@Composable
fun robotoFontFamily(): FontFamily = FontFamily(
    Font(
        resource = Res.font.roboto_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        resource = Res.font.roboto_medium,
        weight = FontWeight.Medium,
    ),
)

@Composable
fun appTypography(): Typography {
    val roboto = robotoFontFamily()

    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = roboto),
        displayMedium = baseline.displayMedium.copy(fontFamily = roboto),
        displaySmall = baseline.displaySmall.copy(fontFamily = roboto),

        headlineLarge = baseline.headlineLarge.copy(fontFamily = roboto),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = roboto),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = roboto),

        titleLarge = baseline.titleLarge.copy(fontFamily = roboto),
        titleMedium = baseline.titleMedium.copy(fontFamily = roboto),
        titleSmall = baseline.titleSmall.copy(fontFamily = roboto),

        labelLarge = baseline.labelLarge.copy(
            fontFamily = roboto,
            fontWeight = FontWeight.Medium,
            fontSize = titleLargeTextSize,
            lineHeight = titleLargeTextSize,
            letterSpacing = 0.sp,
        ),

        labelMedium = baseline.labelMedium.copy(
            fontFamily = roboto,
            fontWeight = FontWeight.Medium,
            fontSize = titleMediumTextSize,
            lineHeight = titleMediumTextSize,
            letterSpacing = 0.sp,
        ),
        labelSmall = baseline.labelSmall.copy(
            fontFamily = roboto,
            fontWeight = FontWeight.Medium,
            fontSize = titleSmallTextSize,
            lineHeight = titleSmallTextSize,
            letterSpacing = 0.sp,
        ),

        bodyLarge = baseline.bodyLarge.copy(
            fontFamily = roboto,
            fontWeight = FontWeight.Light,
            fontSize = bodyLargeTextSize,
            lineHeight = bodyLargeTextSize,
            letterSpacing = 0.sp,
        ),
        bodyMedium = baseline.bodyMedium.copy(
            fontFamily = roboto,
            fontWeight = FontWeight.Light,
            fontSize = bodyMediumTextSize,
            lineHeight = bodyMediumTextSize,
            letterSpacing = 0.sp,
        ),
        bodySmall = baseline.bodySmall.copy(
            fontFamily = roboto,
            fontWeight = FontWeight.Light,
            fontSize = bodySmallTextSize,
            lineHeight = bodySmallTextSize,
            letterSpacing = 0.1.sp,
        ),
    )
}

//  ====================  LocalCustomTypo  =======================
val LocalCustomTypo = compositionLocalOf { CustomTypography() }

data class CustomTypography(
    val topBarTitle: TextStyle = baseline.bodySmall,
    val screenTitle: TextStyle = baseline.bodySmall,
    val screenTitleLong: TextStyle = baseline.bodySmall,
    val textField: TextStyle = baseline.bodySmall,
    val textFieldDisabled: TextStyle = baseline.bodySmall,
    val textFieldLabel: TextStyle = baseline.bodySmall,
    val monthPickerText: TextStyle = baseline.bodySmall,
    val buttonText: TextStyle = baseline.bodySmall,
)

@Composable
fun getCustomTypo() =
    CustomTypography(
        topBarTitle = baseline.bodySmall.copy(
            fontFamily = robotoFontFamily(),
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 20.sp,
            lineHeight = 30.sp,
            letterSpacing = 1.sp,
        ),
        screenTitle = baseline.bodySmall.copy(
            fontFamily = robotoFontFamily(),
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (0.1).sp,
        ),
        screenTitleLong = baseline.bodySmall.copy(
            fontFamily = robotoFontFamily(),
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (0.1).sp,
        ),
        textField = baseline.bodySmall.copy(
            fontFamily = robotoFontFamily(),
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = bodyMediumTextSize,
            lineHeight = 24.sp,
            letterSpacing = (0.1).sp,
        ),
        textFieldDisabled = baseline.bodySmall.copy(
            fontFamily = robotoFontFamily(),
            fontWeight = FontWeight.Light,
            color = TextFieldDefaults.colors().unfocusedPlaceholderColor,
            fontSize = bodyMediumTextSize,
            lineHeight = 20.sp,
            letterSpacing = (0.1).sp,
        ),
        textFieldLabel = baseline.bodySmall.copy(
            fontFamily = robotoFontFamily(),
            fontWeight = FontWeight.Light,
            color = TextFieldDefaults.colors().unfocusedPlaceholderColor,
            fontSize = bodySmallTextSize,
            lineHeight = 16.sp,
            letterSpacing = (0.1).sp,
        ),
        monthPickerText = baseline.bodySmall.copy(
            fontFamily = robotoFontFamily(),
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp,
        ),
        buttonText = baseline.bodySmall.copy(
            fontFamily = robotoFontFamily(),
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = buttonTextSize,
            lineHeight = 24.sp,
            letterSpacing = (0.1).sp,
        ),
    )
