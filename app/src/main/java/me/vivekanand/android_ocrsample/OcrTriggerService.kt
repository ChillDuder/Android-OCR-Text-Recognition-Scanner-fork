package me.vivekanand.android_ocrsample

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class OcrTriggerService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val request = OneTimeWorkRequestBuilder<OcrWorker>()
            .setInputData(workDataOf())
            .build()

        WorkManager.getInstance(this).enqueue(request)
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}