package com.krew18.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.krew18.app.domain.model.ThemeMode

/*
 * Krew18 brand palette: deep lagoon teal (primary), sunset coral (secondary) and
 * warm sand/gold (tertiary), on a sand-tinted light surface or a deep teal-black dark one.
 */
private val Teal40 = Color(0xFF00696B)
private val Teal80 = Color(0xFF4CDADB)
private val Teal90 = Color(0xFF9CF1F1)
private val Teal10 = Color(0xFF002020)
private val Teal20 = Color(0xFF003738)
private val Teal30 = Color(0xFF004F51)

private val Coral40 = Color(0xFFA5401F)
private val Coral80 = Color(0xFFFFB59E)
private val Coral90 = Color(0xFFFFDBD0)
private val Coral10 = Color(0xFF3A0B00)
private val Coral20 = Color(0xFF5E1702)
private val Coral30 = Color(0xFF842A0D)

private val Sand40 = Color(0xFF7A5900)
private val Sand80 = Color(0xFFF0C048)
private val Sand90 = Color(0xFFFFDEA3)
private val Sand10 = Color(0xFF261900)
private val Sand20 = Color(0xFF402D00)
private val Sand30 = Color(0xFF5C4300)

private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    secondary = Coral40,
    onSecondary = Color.White,
    secondaryContainer = Coral90,
    onSecondaryContainer = Coral10,
    tertiary = Sand40,
    onTertiary = Color.White,
    tertiaryContainer = Sand90,
    onTertiaryContainer = Sand10,
    background = Color(0xFFFFF8F2),
    onBackground = Color(0xFF1C1B19),
    surface = Color(0xFFFFF8F2),
    onSurface = Color(0xFF1C1B19),
    surfaceVariant = Color(0xFFDAE5E4),
    onSurfaceVariant = Color(0xFF3F4949),
    outline = Color(0xFF6F7979),
)

private val DarkColors = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal20,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,
    secondary = Coral80,
    onSecondary = Coral20,
    secondaryContainer = Coral30,
    onSecondaryContainer = Coral90,
    tertiary = Sand80,
    onTertiary = Sand20,
    tertiaryContainer = Sand30,
    onTertiaryContainer = Sand90,
    background = Color(0xFF0E1A1B),
    onBackground = Color(0xFFDDE4E3),
    surface = Color(0xFF0E1A1B),
    onSurface = Color(0xFFDDE4E3),
    surfaceVariant = Color(0xFF3F4949),
    onSurfaceVariant = Color(0xFFBEC8C8),
    outline = Color(0xFF889392),
)

private val KrewTypography = Typography().let { base ->
    base.copy(
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.1.sp),
    )
}

private val KrewShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun KrewTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = KrewTypography,
        shapes = KrewShapes,
        content = content,
    )
}
