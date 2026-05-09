package com.saimum.saimummusic

import android.content.Context
import com.yakivmospan.scytale.Crypto
import com.yakivmospan.scytale.Options
import com.yakivmospan.scytale.Store
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

private const val CHANNEL = "com.saimum.saimummusic/legacy_migration"
private const val PREFS_NAME = "com.saimum.saimummusic_apps_settings"
private const val MIGRATION_FLAG = "flutter_migration_completed"

class MigrationChannel(private val context: Context) {

    fun register(flutterEngine: FlutterEngine) {
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL,
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "readLegacyUser" -> handleReadLegacyUser(result)
                "markMigrationComplete" -> handleMarkComplete(result)
                else -> result.notImplemented()
            }
        }
    }

    private fun handleReadLegacyUser(result: MethodChannel.Result) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isLogged = prefs.getBoolean("islogged", false)
            if (!isLogged) {
                result.success(null)
                return
            }

            val store = Store(context)
            val key = store.getSymmetricKey(BuildConfig.ENC_KEY, null)

            fun decrypt(raw: String?): String {
                if (raw.isNullOrEmpty()) return ""
                return try {
                    val crypto = Crypto(Options.TRANSFORMATION_SYMMETRIC)
                    crypto.decrypt(raw, key)
                } catch (e: Exception) {
                    ""
                }
            }

            val data = mapOf(
                "uid" to decrypt(prefs.getString("uid", "")),
                "name" to decrypt(prefs.getString("name", "")),
                "email" to decrypt(prefs.getString("email", "")),
                "mobile" to decrypt(prefs.getString("mobile", "")),
                "loginType" to decrypt(prefs.getString("loginType", "")),
                "auth_id" to decrypt(prefs.getString("auth_id", "")),
                "profile" to decrypt(prefs.getString("profile", "")),
            )
            result.success(data)
        } catch (e: Exception) {
            result.error("MIGRATION_READ_ERROR", e.message, null)
        }
    }

    private fun handleMarkComplete(result: MethodChannel.Result) {
        try {
            context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(MIGRATION_FLAG, true)
                .apply()
            result.success(null)
        } catch (e: Exception) {
            result.error("MIGRATION_FLAG_ERROR", e.message, null)
        }
    }
}
