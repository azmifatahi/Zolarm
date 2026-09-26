package com.zolarm.app.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.zolarm.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Checks GitHub Releases for a newer version tag.
 * Repo: azmifatahi/Zolarm  (adjust if forked)
 */
object UpdateChecker {

    private const val RELEASES_API = "https://api.github.com/repos/azmifatahi/Zolarm/releases"
    private const val RELEASES_PAGE = "https://github.com/azmifatahi/Zolarm/releases"

    data class Result(
        val isLatest: Boolean,
        val currentVersion: String,
        val latestVersion: String?,
        val releaseUrl: String?,
        val error: String? = null
    )

    suspend fun check(): Result = withContext(Dispatchers.IO) {
        val current = BuildConfig.VERSION_NAME
        try {
            val conn = (URL(RELEASES_API).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Zolarm-Android")
            }
            val code = conn.responseCode
            if (code != 200) {
                return@withContext Result(true, current, null, RELEASES_PAGE, "HTTP $code")
            }
            val body = conn.inputStream.bufferedReader().readText()
            val arr = JSONArray(body)
            if (arr.length() == 0) {
                return@withContext Result(true, current, current, RELEASES_PAGE)
            }
            val latest = arr.getJSONObject(0)
            var tag = latest.optString("tag_name", current)
            if (tag.startsWith("v") || tag.startsWith("V")) tag = tag.substring(1)
            val htmlUrl = latest.optString("html_url", RELEASES_PAGE)
            val isLatest = compareVersions(current, tag) >= 0
            Result(isLatest, current, tag, htmlUrl)
        } catch (t: Throwable) {
            Result(true, current, null, RELEASES_PAGE, t.message)
        }
    }

    fun openReleases(context: Context, url: String? = RELEASES_PAGE) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url ?: RELEASES_PAGE))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    /** Returns >0 if a > b, 0 if equal, <0 if a < b (simple numeric segments). */
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
