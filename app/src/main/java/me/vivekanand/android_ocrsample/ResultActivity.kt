package me.vivekanand.android_ocrsample

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

class ResultActivity : AppCompatActivity() {

    private val CHANNEL_ID = "result_activity_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // No UI — no setContentView

        postNotification("ResultActivity started")

        val bitmap = loadJpgAsBitmap("/storage/emulated/0/Download/sample.jpg")
        if (bitmap != null) {
            runCloudVisionOcrOnBitmap(bitmap)
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun loadJpgAsBitmap(path: String): Bitmap? {
        return try {
            val file = File(path)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)
            else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun runCloudVisionOcrOnBitmap(bitmap: Bitmap) {
        val prefs: SharedPreferences = getSharedPreferences("ocr_prefs", MODE_PRIVATE)
        val apiKey = prefs.getString("cloud_vision_api_key", null)

        if (apiKey.isNullOrBlank()) {
            showOcrResult("Cloud OCR", "API key not found.")
            return
        }

        val base64Image = bitmapToBase64(bitmap)

        val jsonRequest = JSONObject().apply {
            put("requests", JSONArray().apply {
                put(JSONObject().apply {
                    put("image", JSONObject().apply {
                        put("content", base64Image)
                    })
                    put("features", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "TEXT_DETECTION")
                        })
                    })
                })
            })
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val mediaType = "application/json".toMediaTypeOrNull()
                val body = jsonRequest.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://vision.googleapis.com/v1/images:annotate?key=$apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val resultText = if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody ?: "")
                    json.getJSONArray("responses")
                        .getJSONObject(0)
                        .optJSONObject("fullTextAnnotation")
                        ?.optString("text") ?: "No text found"
                } else {
                    "HTTP Error: ${response.code}"
                }

                runOnUiThread {
                    showOcrResult("Cloud OCR", resultText)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showOcrResult("Cloud OCR", "Error: ${e.message}")
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun showOcrResult(title: String, message: String) {
        postNotification("$title result: $message")

        val resultIntent = Intent().apply {
            putExtra("ocr_result", message)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun postNotification(contentText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Result Activity Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("ResultActivity")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(this).notify(1001, builder.build())
    }
}
