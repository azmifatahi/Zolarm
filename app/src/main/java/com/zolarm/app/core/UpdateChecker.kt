package com.zolarm.app.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.zolarm.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Auto-update from GitHub Releases.
 * Checks latest release, downloads APK asset, prompts system installer.
 */
object UpdateChecker {

    private const val TAG = "ZolarmUpdate"
    private const val OWNER = "azmifatahi"
    private const val REPO = "Zolarm"
    private const val RELEASES_API = "https://api.github.com/repos/$OWNER/$REPO/releases"
    private const val RELEASES_PAGE = "https://github.com/$OWNER/$REPO/releases"

    data class Result(
        val isLatest: Boolean,
        val currentVersion: String,
        val latestVersion: String?,
        val releaseUrl: String?,
        val apkUrl: String? = null,
        val error: String? = null,
        val downloadedApk: File? = null
    )

    suspend fun check(): Result = withContext(Dispatchers.IO) {
        val current = BuildConfig.VERSION_NAME
        try {
            val body = httpGet(RELEASES_API) ?: return@withContext Result(
                true, current, null, RELEASES_PAGE, error = "network"
            )
            val arr = JSONArray(body)
            if (arr.length() == 0) {
                return@withContext Result(true, current, current, RELEASES_PAGE)
            }
            val latest: JSONObject = arr.getJSONObject(0)
            var tag = latest.optString("tag_name", current)
            if (tag.startsWith("v") || tag.startsWith("V")) tag = tag.substring(1)
            val htmlUrl = latest.optString("html_url", RELEASES_PAGE)
            val isLatest = compareVersions(current, tag) >= 0

            var apkUrl: String? = null
            val assets = latest.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i)
                    val name = a.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = a.optString("browser_download_url", null)
                        break
                    }
                }
            }
            Result(isLatest, current, tag, htmlUrl, apkUrl)
        } catch (t: Throwable) {
            Log.e(TAG, "check failed", t)
            Result(true, current, null, RELEASES_PAGE, error = t.message)
        }
    }

    suspend fun checkAndDownload(context: Context): Result = withContext(Dispatchers.IO) {
        val result = check()
        if (result.isLatest || result.apkUrl.isNullOrBlank()) return@withContext result
        try {
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            val out = File(dir, "Zolarm-update.apk")
            if (out.exists()) out.delete()
            downloadFile(result.apkUrl!!, out)
            Log.i(TAG, "Downloaded update to ${out.absolutePath} size=${out.length()}")
            result.copy(downloadedApk = out)
        } catch (t: Throwable) {
            Log.e(TAG, "download failed", t)
            result.copy(error = t.message)
        }
    }

    fun installApk(context: Context, apk: File): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return false
                }
            }
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                apk
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "install failed", t)
            openReleases(context, null)
            false
        }
    }

    fun openReleases(context: Context, url: String? = RELEASES_PAGE) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url ?: RELEASES_PAGE))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    private fun httpGet(url: String): String? {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Zolarm-Android")
        }
        return if (conn.responseCode == 200) {
            conn.inputStream.bufferedReader().readText()
        } else null
    }

    private fun downloadFile(url: String, dest: File) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Zolarm-Android")
            setRequestProperty("Accept", "application/octet-stream")
        }
        conn.inputStream.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        if (dest.length() < 10_000L) error("APK too small: ${dest.length()}")
    }

    private fun compareVersions(a: String, b: String): Int {
        val pa = a.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }
        val pb = b.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }
        val n = maxOf(pa.size, pb.size)
        for (i in 0 until n) {
            val x = pa.getOrElse(i) { 0 }
            val y = pb.getOrElse(i) { 0 }
            if (x != y) return x - y
        }
        return 0
    }
}
