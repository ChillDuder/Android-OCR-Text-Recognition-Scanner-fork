package me.vivekanand.android_ocrsample;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.Text;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class LatestOcrActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_MLKIT = 101;
    private static final int REQUEST_IMAGE_TESSERACT = 102;
    private static final int REQUEST_IMAGE_CLOUD = 103;
    private String cloudVisionApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest_ocr);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Latest OCR Tech");
        }

        // Remove API key prompt from here
        SharedPreferences prefs = getSharedPreferences("ocr_prefs", MODE_PRIVATE);
        cloudVisionApiKey = prefs.getString("cloud_vision_api_key", null);

        findViewById(R.id.btn_mlkit_ocr).setOnClickListener(v -> runMlKitOcr());
        findViewById(R.id.btn_tesseract_ocr).setOnClickListener(v -> runTesseractOcr());
        findViewById(R.id.btn_cloud_vision_ocr).setOnClickListener(v -> runCloudVisionOcr());
    }

    private void promptForApiKey(SharedPreferences prefs) {
        runOnUiThread(() -> {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            new AlertDialog.Builder(this)
                .setTitle("Enter Cloud Vision API Key")
                .setMessage("Please enter your Google Cloud Vision API key. This will be saved for future use.")
                .setView(input)
                .setCancelable(true)
                .setPositiveButton("Save", (dialog, which) -> {
                    String key = input.getText().toString().trim();
                    if (!key.isEmpty()) {
                        prefs.edit().putString("cloud_vision_api_key", key).apply();
                        cloudVisionApiKey = key;
                        Toast.makeText(this, "API key saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "API key cannot be empty.", Toast.LENGTH_SHORT).show();
                        promptForApiKey(prefs);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
        });
    }

    private void runMlKitOcr() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_MLKIT);
    }

    private void runTesseractOcr() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_TESSERACT);
    }

    private void runCloudVisionOcr() {
        SharedPreferences prefs = getSharedPreferences("ocr_prefs", MODE_PRIVATE);
        cloudVisionApiKey = prefs.getString("cloud_vision_api_key", null);
        if (cloudVisionApiKey == null || cloudVisionApiKey.isEmpty()) {
            promptForApiKey(prefs);
            Toast.makeText(this, "Please enter your Cloud Vision API key first.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_CLOUD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                if (requestCode == REQUEST_IMAGE_MLKIT) {
                    runMlKitOcrOnBitmap(bitmap);
                } else if (requestCode == REQUEST_IMAGE_TESSERACT) {
                    runTesseractOcrOnBitmap(bitmap);
                } else if (requestCode == REQUEST_IMAGE_CLOUD) {
                    runCloudVisionOcrOnBitmap(bitmap);
                }
            } catch (Exception e) {
                showOcrResult("Error", e.getMessage());
            }
        }
    }

    // ML Kit OCR logic
    private void runMlKitOcrOnBitmap(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(new TextRecognizerOptions.Builder().build());
        recognizer.process(image)
            .addOnSuccessListener(visionText -> showOcrResult("ML Kit OCR", visionText.getText()))
            .addOnFailureListener(e -> showOcrResult("ML Kit OCR", "Failed: " + e.getMessage()));
    }

    // Tesseract OCR logic
    private void runTesseractOcrOnBitmap(Bitmap bitmap) {
        try {
            // Prepare tessdata if not present
            File dir = getFilesDir();
            File tessData = new File(dir, "tessdata/eng.traineddata");
            if (!tessData.exists()) {
                tessData.getParentFile().mkdirs();
                InputStream is = getAssets().open("tessdata/eng.traineddata");
                FileOutputStream fos = new FileOutputStream(tessData);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.close();
                is.close();
            }
            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init(dir.getAbsolutePath(), "eng");
            tessBaseAPI.setImage(bitmap);
            String result = tessBaseAPI.getUTF8Text();
            tessBaseAPI.end();
            showOcrResult("Tesseract OCR", result);
        } catch (Exception e) {
            showOcrResult("Tesseract OCR", "Error: " + e.getMessage());
        }
    }

    // Cloud Vision OCR logic
    private void runCloudVisionOcrOnBitmap(Bitmap bitmap) {
        if (cloudVisionApiKey == null || cloudVisionApiKey.isEmpty()) {
            promptForApiKey(getSharedPreferences("ocr_prefs", MODE_PRIVATE));
            showOcrResult("Cloud Vision OCR", "API key required. Please try again after entering your key.");
            return;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
            new Thread(() -> {
                try {
                    String json = "{ \"requests\": [ { \"image\": { \"content\": \"" + base64 + "\" }, \"features\": [ { \"type\": \"TEXT_DETECTION\" } ] } ] }";
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
                    Request request = new Request.Builder()
                        .url("https://vision.googleapis.com/v1/images:annotate?key=" + cloudVisionApiKey)
                        .post(body)
                        .build();
                    Response response = client.newCall(request).execute();
                    String result = response.body().string();
                    final String ocrText;
                    if (result.contains("textAnnotations")) {
                        java.util.regex.Matcher matcher = java.util.regex.Pattern
                            .compile("\"description\"\\s*:\\s*\"(.*?)\"")
                            .matcher(result);
                        if (matcher.find()) {
                            ocrText = matcher.group(1);
                        } else {
                            ocrText = "No text found";
                        }
                    } else {
                        ocrText = "No text found";
                    }
                    runOnUiThread(() -> showOcrResult("Cloud Vision OCR", ocrText));
                } catch (Exception e) {
                    runOnUiThread(() -> showOcrResult("Cloud Vision OCR", "Error: " + e.getMessage()));
                }
            }).start();
        } catch (Exception e) {
            showOcrResult("Cloud Vision OCR", "Error: " + e.getMessage());
        }
    }

    // Show OCR result in a dialog
    private void showOcrResult(String title, String result) {
        runOnUiThread(() -> new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(result)
            .setPositiveButton("OK", null)
            .show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_latest_ocr, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_update_api_key) {
            promptForApiKey(getSharedPreferences("ocr_prefs", MODE_PRIVATE));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 