import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:crypto/crypto.dart';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final apiClientProvider = Provider<Dio>((ref) => ApiClient.instance.dio);

// For physical devices, use your PC's IP address
// For Android Emulator, use 10.0.2.2
const _baseIp = '192.168.0.170'; 
const _baseUrl = 'http://$_baseIp:8000/api/';
const _connectTimeoutMs = 15000;
const _receiveTimeoutMs = 60000;

// Placeholder — move to BuildConfig / flutter_secure_storage in Phase 8.
const _hmacSecret = 'your-hmac-signing-secret';

class ApiClient {
  ApiClient._();
  static final ApiClient instance = ApiClient._();

  late final Dio _dio = _buildDio();

  Dio get dio => _dio;

  Dio _buildDio() {
    final dio = Dio(
      BaseOptions(
        baseUrl: _baseUrl,
        connectTimeout: const Duration(milliseconds: _connectTimeoutMs),
        receiveTimeout: const Duration(milliseconds: _receiveTimeoutMs),
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
        },
      ),
    );

    // dio.interceptors.add(HmacSignatureInterceptor()); // Disabled for now
    dio.interceptors.add(
      LogInterceptor(
        request: true,
        requestBody: true,
        responseBody: true,
        responseHeader: false,
        error: true,
        logPrint: (o) => _log(o.toString()),
      ),
    );

    return dio;
  }

  void setAuthToken(String token) {
    _dio.options.headers['Authorization'] = 'Bearer $token';
  }

  void clearAuthToken() {
    _dio.options.headers.remove('Authorization');
  }

  static void _log(String message) {
    // Replace with structured logger in Phase 8.
    debugPrint('[ApiClient] $message');
  }
}

// ---------------------------------------------------------------------------
// HMAC-SHA256 Request Signing Interceptor
// Adds X-Timestamp, X-Nonce, X-Signature to every outgoing request.
// Server must verify: signature matches, timestamp is within 60 seconds.
// ---------------------------------------------------------------------------

class HmacSignatureInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    final timestamp = DateTime.now().millisecondsSinceEpoch ~/ 1000;
    final nonce = _nonce();
    final body = _serializeBody(options.data);
    final message =
        '${options.method.toUpperCase()}\n${options.path}\n$timestamp\n$nonce\n$body';

    options.headers['X-Timestamp'] = timestamp.toString();
    options.headers['X-Nonce'] = nonce;
    options.headers['X-Signature'] = _sign(message);

    handler.next(options);
  }

  String _sign(String message) {
    final key = utf8.encode(_hmacSecret);
    final bytes = utf8.encode(message);
    return Hmac(sha256, key).convert(bytes).toString();
  }

  String _nonce() {
    final bytes = List<int>.generate(
      16,
      (_) => DateTime.now().microsecondsSinceEpoch & 0xFF,
    );
    return base64Url.encode(bytes).replaceAll('=', '');
  }

  String _serializeBody(dynamic data) {
    if (data == null) return '';
    if (data is String) return data;
    try {
      return jsonEncode(data);
    } catch (_) {
      return '';
    }
  }
}
