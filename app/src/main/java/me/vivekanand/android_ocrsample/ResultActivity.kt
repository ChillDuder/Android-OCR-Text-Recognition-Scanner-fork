package me.vivekanand.android_ocrsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val workRequest = OneTimeWorkRequestBuilder<OcrWorker>()
            .setInputData(workDataOf())
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)

        finish()
    }
}