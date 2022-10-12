package com.example.zygos.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.zygos.R

val EczarFontFamily = FontFamily(
    Font(R.font.eczar_regular),
    Font(R.font.eczar_semibold, FontWeight.SemiBold)
)
val RobotoCondensed = FontFamily(
    Font(R.font.robotocondensed_regular),
    Font(R.font.robotocondensed_light, FontWeight.Light),
    Font(R.font.robotocondensed_bold, FontWeight.Bold)
)
val Roboto = FontFamily(
    Font(R.font.roboto_regular),
    Font(R.font.roboto_bold, FontWeight.Bold)
)
val UmTypewriter = FontFamily(
    Font(R.font.um_typewriter),
)

val Typography = Typography(
    defaultFontFamily = RobotoCondensed,
    h1 = TextStyle(
        fontWeight = FontWeight.W100,
        fontSize = 96.sp,
    ),
    // Account selection
    h2 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        fontFamily = EczarFontFamily,
        letterSpacing = 1.5.sp,
        lineHeight = 26.sp,
    ),
    // List titles
    h3 = TextStyle(
        fontWeight = FontWeight.W100,
        fontSize = 22.sp,
        fontFamily = EczarFontFamily,
    ),
    h4 = TextStyle(
        fontWeight = FontWeight.W700,
        fontSize = 34.sp,
        fontFamily = Roboto,
    ),
    // Option menu title, add account
    h5 = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 22.sp,
        fontFamily = Roboto,
    ),
    h6 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 20.sp,
        fontFamily = EczarFontFamily,
        letterSpacing = 3.sp
    ),
    subtitle1 = TextStyle( // Subtitle in ticker rows
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 3.sp
    ),
    subtitle2 = TextStyle( // Monospaced for grids/etc. TODO replace usage with overline
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        fontFamily = UmTypewriter,
        letterSpacing = 0.1.em
    ),
    body1 = TextStyle( // Default text, ticker row, options menu
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.1.em
    ),
    body2 = TextStyle( // Transactions
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.em
    ),
    button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.em
    ),
    caption = TextStyle( // Monospaced, i.e. apiKey
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        fontFamily = UmTypewriter,
        letterSpacing = 0.25.em
    ),
    overline = TextStyle( // Monospaced for grids/etc.
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        fontFamily = UmTypewriter,
        letterSpacing = 0.1.em
    ),
)