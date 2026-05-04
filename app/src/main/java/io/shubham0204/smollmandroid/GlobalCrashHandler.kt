/*
 * Copyright (C) 2025 AMM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package io.shubham0204.smollmandroid

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Catches ANY uncaught exception anywhere in the app (including Activity startup crashes)
 * and writes it to a persistent file before the process dies.
 *
 * Usage: Call GlobalCrashHandler.install(application) in SmolChatApplication.onCreate()
 */
object GlobalCrashHandler {

    private const val TAG = "GlobalCrashHandler"
    private const val CRASH_FILE_NAME = "global_crashes.txt"
    private const val MAX_LOG_SIZE_BYTES = 1 * 1024 * 1024 // 1 MB cap

    fun install(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = File(context.filesDir, CRASH_FILE_NAME)
                synchronized(this) {
                    if (file.exists() && file.length() > MAX_LOG_SIZE_BYTES) {
                        file.delete()
                    }
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
                    file.appendText("""
                        |========================================
                        |CRASH @ $timestamp
                        |Thread: ${thread.name}
                        |Exception: ${throwable.javaClass.name}: ${throwable.message}
                        |Stack trace:
                        |${throwable.stackTraceToString()}
                        |========================================
                        |
                    """.trimMargin() + "\n")
                }
                Log.e(TAG, "Uncaught exception logged to $CRASH_FILE_NAME", throwable)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log crash", e)
            }
            // Always chain to the default handler so the app still crashes normally
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    suspend fun readLogs(context: Context): String = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, CRASH_FILE_NAME)
        if (file.exists()) file.readText() else "No global crash logs yet."
    }

    fun clearLogs(context: Context) {
        File(context.filesDir, CRASH_FILE_NAME).delete()
    }

    fun appendDebug(context: Context, tag: String, message: String) {
        try {
            val file = File(context.filesDir, CRASH_FILE_NAME)
            synchronized(this) {
                if (file.exists() && file.length() > MAX_LOG_SIZE_BYTES) file.delete()
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
                file.appendText("[DEBUG $timestamp] [$tag] $message\n")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to append debug log", e)
        }
    }
}
