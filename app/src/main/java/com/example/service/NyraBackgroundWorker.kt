package com.example.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log

class NyraBackgroundWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Nyra background assistant periodic check executed")
        return Result.success()
    }

    companion object {
        private const val TAG = "NyraBackgroundWorker"
    }
}
