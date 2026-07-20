package com.farm.layermanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider

private val LightColors = lightColorScheme(
    primary = OliveDeep,
    onPrimary = StrawSurface,
    primaryContainer = OliveDeepContainer,
    onPrimaryContainer = OliveOnContainer,

    secondary = YolkAmber,
    onSecondary = CharcoalText,
    secondaryContainer = YolkAmberContainer,
    onSecondaryContainer = YolkOnContainer,

    error = BarnRed,
    onError = StrawSurface,
    errorContainer = BarnRedContainer,
    onErrorContainer = BarnOnContainer,

    background = StrawBackground,
    onBackground = CharcoalText,
    surface = StrawSurface,
    onSurface = CharcoalText,
    surfaceVariant = StrawSurfaceVariant,
    onSurfaceVariant = CharcoalMuted
)

private val DarkColors = darkColorScheme(
    primary = OliveDeepDark,
    onPrimary = OliveOnContainerDark,
    primaryContainer = OliveDeepContainerDark,
    onPrimaryContainer = OliveDeepDark,

    secondary = YolkAmberDark,
    onSecondary = YolkOnContainerDark,
    secondaryContainer = YolkAmberContainerDark,
    onSecondaryContainer = YolkAmberDark,

    error = BarnRedDark,
    onError = BarnOnContainerDark,
    errorContainer = BarnRedContainerDark,
    onErrorContainer = BarnRedDark,

    background = CharcoalBackgroundDark,
    onBackground = StrawTextDark,
    surface = CharcoalSurfaceDark,
    onSurface = StrawTextDark,
    surfaceVariant = CharcoalSurfaceVariantDark,
    onSurfaceVariant = StrawMutedDark
)

/**
 * التطبيق عربي بالكامل → RTL دائماً بغض النظر عن لغة النظام، لأن كل المحتوى (تسميات، تقارير) عربي.
 * dynamicColor مُعطَّل عمداً: الهوية اللونية المدروسة (زيتوني/عنبر/أحمر حظيرة) أهم من تكيّف الخلفية
 * مع خلفية شاشة المستخدم (Material You)، لأن التطبيق يُستخدم مهنياً ويحتاج تمييزاً بصرياً ثابتاً
 * لحالات التنبيه (أحمر الحظيرة) عبر كل الأجهزة.
 */
@Composable
fun LayerFarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LayerFarmTypography,
            content = content
        )
    }
}
