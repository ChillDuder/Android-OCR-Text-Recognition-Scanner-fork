package me.vivekanand.android_ocrsample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class OcrTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val request = OneTimeWorkRequestBuilder<OcrWorker>()
            .setInputData(workDataOf())
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}