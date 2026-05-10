/// Spotify-style compact counts: 892, 1.2k, 58k, 1.2M, 5.0B
String formatCompactPlayCount(int n) {
  if (n < 0) return '0';
  if (n < 1000) return n.toString();
  if (n < 1e6) {
    return _suffix(n, 1000, 'k');
  }
  if (n < 1e9) {
    return _suffix(n, 1000000, 'M');
  }
  return _suffix(n, 1000000000, 'B');
}

String _suffix(int n, int divisor, String letter) {
  final v = n / divisor;
  if (v >= 100) {
    return '${v.floor()}$letter';
  }
  final s = v >= 10 ? v.toStringAsFixed(0) : v.toStringAsFixed(1);
  return '${s.replaceAll(RegExp(r'\.0$'), '')}$letter';
}
