package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset

fun getAccentColor(name: String): Color {
    return when (name.lowercase()) {
        "teal" -> Color(0xFF14B8A6)
        "rose" -> Color(0xFFF43F5E)
        "violet" -> Color(0xFF8B5CF6)
        "amber" -> Color(0xFFF59E0B)
        "cyan" -> Color(0xFF22D3EE)
        "emerald" -> Color(0xFF34D399)
        else -> Color(0xFF6366F1) // Indigo
    }
}

fun Modifier.immersiveBackground(accentColor: String = "indigo"): Modifier = this.drawBehind {
    // Solid base background
    drawRect(color = Color(0xFF050507))

    val color1 = getAccentColor(accentColor)
    val color2 = when (accentColor.lowercase()) {
        "indigo" -> Color(0xFFF59E0B) // Amber
        "teal" -> Color(0xFF8B5CF6) // Violet
        "rose" -> Color(0xFF22D3EE) // Cyan
        "violet" -> Color(0xFF34D399) // Emerald
        "amber" -> Color(0xFF14B8A6) // Teal
        "cyan" -> Color(0xFFF43F5E) // Rose
        "emerald" -> Color(0xFF6366F1) // Indigo
        else -> Color(0xFFF59E0B)
    }

    // Top-left background glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color1.copy(alpha = 0.14f), Color.Transparent),
            center = Offset(x = 0f, y = 0f),
            radius = size.width * 0.85f
        ),
        center = Offset(x = 0f, y = 0f),
        radius = size.width * 0.85f
    )

    // Bottom-right background glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color2.copy(alpha = 0.08f), Color.Transparent),
            center = Offset(x = size.width, y = size.height),
            radius = size.width * 0.75f
        ),
        center = Offset(x = size.width, y = size.height),
        radius = size.width * 0.75f
    )
}

private val DarkColorScheme =
  darkColorScheme(
    primary = Indigo400,
    secondary = Teal400,
    tertiary = Rose500,
    background = Slate950,
    surface = Slate900,
    onPrimary = Slate950,
    onSecondary = Slate950,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Slate800,
    onSurfaceVariant = Color.White.copy(alpha = 0.8f)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Indigo500,
    secondary = Teal500,
    tertiary = Rose500,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Slate700
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  accentColor: String = "indigo",
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Update the global package-level variables so static references automatically fetch the current color
  val primaryColor = getAccentColor(accentColor)
  val secondaryColor = when (accentColor.lowercase()) {
      "indigo" -> Color(0xFF14B8A6) // Teal
      "teal" -> Color(0xFF6366F1) // Indigo
      "rose" -> Color(0xFF22D3EE) // Cyan
      "violet" -> Color(0xFF34D399) // Emerald
      "amber" -> Color(0xFF14B8A6) // Teal
      "cyan" -> Color(0xFFF43F5E) // Rose
      "emerald" -> Color(0xFF6366F1) // Indigo
      else -> Color(0xFF14B8A6)
  }

  currentAccentColor = primaryColor
  currentSecondaryColor = secondaryColor

  val colorScheme = if (darkTheme) {
      darkColorScheme(
          primary = primaryColor,
          secondary = secondaryColor,
          tertiary = Rose500,
          background = Slate950,
          surface = Slate900,
          onPrimary = Slate950,
          onSecondary = Slate950,
          onBackground = Color.White,
          onSurface = Color.White,
          surfaceVariant = Slate800,
          onSurfaceVariant = Color.White.copy(alpha = 0.8f)
      )
  } else {
      lightColorScheme(
          primary = primaryColor,
          secondary = secondaryColor,
          tertiary = Rose500,
          background = Color(0xFFF8FAFC),
          surface = Color.White,
          onPrimary = Color.White,
          onSecondary = Color.White,
          onBackground = Slate900,
          onSurface = Slate900,
          surfaceVariant = Color(0xFFF1F5F9),
          onSurfaceVariant = Slate700
      )
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
