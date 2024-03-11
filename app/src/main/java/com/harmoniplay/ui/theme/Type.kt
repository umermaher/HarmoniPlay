package com.harmoniplay.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.harmoniplay.R

val boldFontFamily = FontFamily(Font(R.font.avenir_roman_bold))
val defaultFontFamily = FontFamily(Font(R.font.avenirltstd_roman))
private val defaultTypography = Typography()
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = boldFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = boldFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = boldFontFamily),

//    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = defaultFontFamily),
//    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = defaultFontFamily),
//    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = defaultFontFamily),

    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = boldFontFamily,
    ),
    titleMedium = defaultTypography.titleMedium.copy(
        fontFamily = defaultFontFamily,
        fontSize = 19.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    titleSmall = defaultTypography.titleSmall.copy(
        fontFamily = defaultFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = defaultFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = defaultFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = defaultFontFamily),

    labelLarge = defaultTypography.labelLarge.copy(
        fontFamily = defaultFontFamily,
        fontSize = 15.sp,
    ),
    labelMedium = defaultTypography.labelMedium.copy(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Bold
    ),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = defaultFontFamily)
)
