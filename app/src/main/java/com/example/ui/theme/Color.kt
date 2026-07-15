package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val Slate950 = Color(0xFF050507)
val Slate900 = Color(0xFF0E0E14)
val Slate800 = Color(0xFF161622)
val Slate700 = Color(0xFF232333)

private var _currentAccentColor = Color(0xFF6366F1)
private var _currentSecondaryColor = Color(0xFF14B8A6)

var currentAccentColor: Color
    get() = _currentAccentColor
    set(value) { _currentAccentColor = value }

var currentSecondaryColor: Color
    get() = _currentSecondaryColor
    set(value) { _currentSecondaryColor = value }

val Indigo500: Color get() = currentAccentColor
val Indigo400: Color get() = currentAccentColor
val Indigo300: Color get() = currentAccentColor.copy(alpha = 0.8f)

val Teal500: Color get() = currentSecondaryColor
val Teal400: Color get() = currentSecondaryColor

val Rose500 = Color(0xFFF43F5E)
val Violet500 = Color(0xFF8B5CF6)
val Amber500 = Color(0xFFF59E0B)
val Cyan400 = Color(0xFF22D3EE)
val Emerald400 = Color(0xFF34D399)

// Glassmorphic Helper Colors
val GlassBackground = Color(0xFFFFFFFF).copy(alpha = 0.05f)
val GlassBorder: Color get() = currentAccentColor.copy(alpha = 0.22f)
val GlassOverlay: Color get() = currentAccentColor.copy(alpha = 0.04f)

// Legacy colors to maintain compatibility
val Purple80 = Indigo300
val PurpleGrey80 = Color(0xFF94A3B8)
val Pink80 = Color(0xFFFDA4AF)

val Purple40 = Indigo500
val PurpleGrey40 = Slate700
val Pink40 = Rose500

