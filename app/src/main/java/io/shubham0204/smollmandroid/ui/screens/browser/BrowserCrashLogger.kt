/*
 * Copyright (C) 2025 AMM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package io.shubham0204.smollmandroid.ui.screens.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Persistent crash and console log storage for the browser.
 * Survives renderer crashes because it writes to app-local disk, not RAM.
 */
object BrowserCrashLogger {

    private const val TAG = "BrowserCrashLogger"
    private const val LOG_FILE_NAME = "browser_crash_logs.txt"
    private const val MAX_LOG_SIZE_BYTES = 2 * 1024 * 1024 // 2 MB cap

    private fun getLogFile(context: Context): File =
        File(context.filesDir, LOG_FILE_NAME)

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())

    /**
     * Append a raw line to the log file (thread-safe via synchronized block).
     */
    fun append(context: Context, level: String, message: String) {
        try {
            val file = getLogFile(context)
            synchronized(this) {
                // Rotate if oversized
                if (file.exists() && file.length() > MAX_LOG_SIZE_BYTES) {
                    file.delete()
                }
                file.appendText("[${timestamp()}] [$level] $message\n")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write crash log", e)
        }
    }

    /**
     * Log a JavaScript console message.
     */
    fun logConsole(context: Context, level: String, message: String, source: String? = null) {
        val src = source?.let { " | src=$it" } ?: ""
        append(context, "JS-$level", "$message$src")
    }

    /**
     * Log a renderer / native crash event.
     */
    fun logCrash(context: Context, url: String, details: String? = null) {
        val detail = details?.let { " | $it" } ?: ""
        append(context, "CRASH", "url=$url$detail")
    }

    /**
     * Log navigation events to help with debugging.
     */
    fun logNavigation(context: Context, url: String) {
        append(context, "NAV", url)
    }

    /**
     * Read the entire log contents.
     */
    suspend fun readLogs(context: Context): String = withContext(Dispatchers.IO) {
        val file = getLogFile(context)
        if (file.exists()) {
            file.readText()
        } else {
            "No crash logs yet."
        }
    }

    /**
     * Clear all stored logs.
     */
    fun clearLogs(context: Context) {
        getLogFile(context).delete()
    }

    /**
     * Share the log file via system share sheet.
     */
    fun shareLogs(context: Context) {
        val file = getLogFile(context)
        if (!file.exists() || file.length() == 0L) {
            return
        }
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "AMM Browser Crash Logs")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share crash logs")
        context.startActivity(chooser)
    }
}
