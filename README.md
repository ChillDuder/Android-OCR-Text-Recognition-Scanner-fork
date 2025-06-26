<!--
Android OCR Sample App | Optical Character Recognition | ML Kit | Tesseract | Google Cloud Vision | Open Source | Java | Kotlin | Mobile App | Image to Text
-->

# Android OCR Sample App – Optical Character Recognition for Android (ML Kit, Tesseract, Cloud Vision)

**Meta Description:**
> Android OCRSample is an open-source Android app that demonstrates Optical Character Recognition (OCR) using Google ML Kit, Tesseract, and Google Cloud Vision. Easily extract text from images using your mobile device. Built with Java and Kotlin. Perfect for developers looking for an OCR sample app or image-to-text solution.

---

## Overview: Android OCR, Text Recognition & Image to Text

**Android-OCRSample** is a powerful and easy-to-use Android application for Optical Character Recognition (OCR). This open-source project showcases how to extract text from images using three leading OCR engines:
- **Google ML Kit Text Recognition API** (modern, on-device, fast)
- **Tesseract OCR** (legacy, open-source, highly customizable)
- **Google Cloud Vision API** (cloud-based, supports many languages)

Whether you want to build a document scanner, digitize receipts, or add text recognition to your mobile app, this project is a perfect starting point.

---

## Key Features
- **Android OCR**: Extract text from photos or gallery images
- **Multiple OCR Engines**: ML Kit, Tesseract, and Cloud Vision
- **Modern Material UI**: Clean, user-friendly interface
- **Multi-language Support**: English by default, extensible for other languages
- **Open Source**: Free to use and modify (Apache-2.0 License)
- **Built with Java & Kotlin**: Follows Android best practices
- **Image to Text**: Convert images to editable text on your phone

---

## Screenshots
*Add screenshots or GIFs of the OCR app in action for better SEO and user engagement*

---

## Getting Started: Setup & Installation

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK (API 23+)
- Java 11 or higher (uses Java 17 toolchain)

### Clone & Open
```sh
git clone <this-repo-url>
cd Android-OCRSample
```
Open the project in Android Studio.

### Build & Run
- Connect an Android device or use an emulator
- Click **Run** in Android Studio

### Permissions Required
- Camera
- Read/Write External Storage

### Main Dependencies
- [Google ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition/android)
- [Tesseract (tess-two)](https://github.com/adaptech-cz/Tesseract4Android)
- [Google Cloud Vision API](https://cloud.google.com/vision/docs/ocr)
- [OkHttp](https://square.github.io/okhttp/)
- AndroidX, Material Components

---

## How to Use the Android OCR Sample App
- Launch the app on your Android device
- Choose your OCR engine: ML Kit, Tesseract, or Cloud Vision
- Select an image from the gallery or take a new photo
- The app will extract and display the recognized text
- For Google Cloud Vision, enter your API key when prompted ([Cloud Vision docs](https://cloud.google.com/vision/docs/ocr))

### Adding More Languages for Tesseract
- English (`eng.traineddata`) is included by default
- To support other languages, add the relevant `.traineddata` files to `app/src/main/assets/tessdata/`
  - Download language data from [tessdata repository](https://github.com/tesseract-ocr/tessdata)

---

## Documentation & Resources
- [Google ML Kit Text Recognition for Android](https://developers.google.com/ml-kit/vision/text-recognition/android)
- [Tesseract OCR Engine](https://github.com/tesseract-ocr/tesseract)
- [Tess-Two Android Wrapper](https://github.com/adaptech-cz/Tesseract4Android)
- [Google Cloud Vision OCR](https://cloud.google.com/vision/docs/ocr)
- [Android Camera Documentation](https://developer.android.com/training/camera)

---

## Project Structure
- `app/src/main/java/me/vivekanand/android_ocrsample/` — Main app code (Java/Kotlin)
- `app/src/main/assets/tessdata/` — Tesseract language data files
- `app/src/main/res/layout/` — Android UI layouts
- `app/build.gradle` — Gradle dependencies

---

## License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0). You are free to use, modify, and distribute this Android OCR sample app.

---

## About the Developer
- Developed by **Vivekanand**
- Website: [https://www.vivekanand.me/](https://www.vivekanand.me/)
- For questions or feedback, see the "Note By Developer" section in the app or contact via website.

---

## Contributing
Pull requests and suggestions are welcome! Please open an issue or PR for improvements.

---

## Keywords
Android OCR, Optical Character Recognition, Text Recognition, Tesseract, ML Kit, Google Cloud Vision, open source, Java, Kotlin, mobile app, image to text, OCR sample app, Android text recognition, document scanner, OCR Android example, OCR library, OCR open source Android

