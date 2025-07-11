package me.vivekanand.android_ocrsample

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

class OcrWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val imagePath = "/sdcard/NonSync/gctemp/g.jpg"
//    private val imagePath = "/storage/emulated/0/NonSync/gctemp/g.jpg"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = applicationContext.getSharedPreferences("ocr_prefs", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("cloud_vision_api_key", null)

        if (apiKey.isNullOrBlank()) {
            notifyError("API key not found. #GCERR2")
            sendResultBroadcastToAutomagic("error")
            return@withContext Result.failure()
        }
        val bitmap = loadBitmap(imagePath)

        if (bitmap == null) {
            //notifyError("Image not found or unreadable. #GCERR1")
            sendResultBroadcastToAutomagic("error")
            return@withContext Result.failure()
        }

        val base64Image = encodeToBase64(bitmap)

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

        val client = OkHttpClient.Builder()
            .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val body = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://vision.googleapis.com/v1/images:annotate?key=$apiKey")
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                notifyError("HTTP error: ${response.code} #GCERR3")
                sendResultBroadcastToAutomagic("error")
                return@withContext Result.failure()
            }

            val json = JSONObject(response.body?.string() ?: "")
            val resultText = json.getJSONArray("responses")
                .getJSONObject(0)
                .optJSONObject("fullTextAnnotation")
                ?.optString("text") ?: "No text found"

            sendResultBroadcastToAutomagic(resultText)
            Result.success()

        } catch (e: java.net.SocketTimeoutException) {
            sendResultBroadcastToAutomagic("timeout")
            notifyError("OCR request timed out after 5s. #GCERR5")
            Result.success()
        } catch (e: Exception) {
            notifyError("Exception: ${e.message} #GCERR4")
            sendResultBroadcastToAutomagic("error")
            Result.failure()
        }
    }

    private fun loadBitmap(path: String): Bitmap? {
        return try {
            val file = File(path)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } catch (e: Exception) {
            Log.e("OcrWorker", "Error loading bitmap: ${e.message}")
            null
        }
    }

    private fun encodeToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun sendResultBroadcastToAutomagic(text: String) {
        val intent = Intent("com.yourapp.OCR_RESULT").apply {
            `package` = "ch.gridvision.ppam.androidautomagic"
            putExtra("ocr_result", text)
        }
        applicationContext.sendBroadcast(intent)
    }

    private fun notifyError(message: String) {
        NotificationHelper.postErrorNotification(applicationContext, message)
    }
}