package me.vivekanand.android_ocrsample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imagePath = "/storage/emulated/0/NonSync/gctemp/g.jpg"
        val bitmap = loadJpgAsBitmap(imagePath)

        if (bitmap != null) {
            runCloudVisionOcrOnBitmap(bitmap)
        } else {
            showError("Image not found or unreadable. #GCERR1")
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
            showError("API key not found. #GCERR2")
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
                    showError("HTTP error: ${response.code} #GCERR3")
                    return@launch
                }

                runOnUiThread {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra("ocr_result", resultText)
                    })
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showError("Exception: ${e.message} #GCERR4")
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

    private fun showError(message: String) {
        NotificationHelper.postErrorNotification(this, message)

        setResult(Activity.RESULT_CANCELED, Intent().apply {
            putExtra("ocr_result", message)
        })
        finish()
    }
}
