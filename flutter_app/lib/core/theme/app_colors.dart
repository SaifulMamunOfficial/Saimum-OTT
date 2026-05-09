import 'package:flutter/material.dart';

abstract final class AppColors {
  // ── Backgrounds ────────────────────────────────────────────────────────────
  static const background = Color(0xFF0A0A0B); // Deep Obsidian
  static const surfaceOne = Color(0xFF111114); // Slightly lifted surface
  static const surfaceTwo = Color(0xFF1A1A1F); // Card/sheet base

  // ── Brand ──────────────────────────────────────────────────────────────────
  static const primary = Color(0xFF00F2FF); // Neon Cyan
  static const primaryDim = Color(0xFF00B8CC); // Dimmed Cyan
  static const accent = Color(0xFF8B5CF6);   // Vibrant Violet
  static const accentDim = Color(0xFF6D3FCF); // Dimmed Violet

  // ── Semantic ───────────────────────────────────────────────────────────────
  static const error = Color(0xFFFF453A);
  static const success = Color(0xFF34C759);
  static const warning = Color(0xFFFF9F0A);

  // ── Text ───────────────────────────────────────────────────────────────────
  static const onPrimary = Color(0xFF000000);
  static const onSurface = Color(0xFFEAEAEA);
  static const onSurfaceMuted = Color(0xFF8A8A9A);

  // ── Glass ──────────────────────────────────────────────────────────────────
  static const glassFill = Color(0x12FFFFFF);    // 7% white
  static const glassBorder = Color(0x26FFFFFF);  // 15% white
  static const glassHighlight = Color(0x0DFFFFFF); // 5% white
}
