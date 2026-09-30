# 🩺 AI-Based Health Monitoring System

An end-to-end Android mobile application and Machine Learning backend system that tracks real-time vital health parameters (Heart Rate, Blood Pressure, Step Count, Sleep Duration) and predicts **Health Risk Levels** and **Stress Levels** using trained **Random Forest Classifiers**.

---

### 📥 [Download Latest APK (ZIP)](https://github.com/arjun250604/health-monitoring-app/raw/main/apk/HealthMonitor.zip)

---

## 🌟 Features

- 📱 **Modern Android App**: Built with Jetpack Compose (Material 3), featuring custom health metrics cards, live pulse sync indicators, and dark/light mode UI.
- ⌚ **Wearable Integration**: Seamlessly syncs vitals (Heart Rate, Blood Pressure, Steps, Sleep) automatically via **Android Health Connect**, supporting devices like Pixel Watch, Galaxy Watch, Fitbit, Garmin, and more.
- ✍️ **Manual Entry Mode**: Option to manually input vitals when a wearable device is not available.
- 🤖 **Machine Learning Intelligence**: Random Forest Classifiers trained on health parameters to dynamically predict:
  - **Risk Level**: `Low`, `Medium`, or `High`
  - **Stress Level**: `Normal`, `Moderate`, or `High`
- 🌐 **Cloud ML Server**: Flask REST API deployed as a serverless backend on **Vercel** (`https://backend-lime-xi-18.vercel.app`).
- ⚡ **Real-Time Data Streaming**: Asynchronous HTTP client using **Retrofit**, **OkHttp**, and **Coroutines** with automatic offline fallback.
- 📦 **Standalone Android APK**: Pre-compiled `.apk` package ready for installation on any physical Android device.

---

## 🏗 System Architecture

```
+-------------------+     +------------------------------------+        POST /predict        +-----------------------------------+
|    Smartwatch     | --> |         Android Mobile App         | --------------------------> |        Flask ML Backend           |
| (via Health Conn) |     |      (Jetpack Compose Dashboard)   |                             |      (Vercel Cloud Server)        |
+-------------------+     |                                    |                             |                                   |
                          | - Heart Rate (BPM)                 |                             | 1. StandardScaler (scaler.pkl)    |
+-------------------+     | - Blood Pressure (mmHg)            |                             | 2. Risk Model (risk_model.pkl)    |
|   Manual Input    | --> | - Steps (10k Goal Tracking)        |                             | 3. Stress Model (stress_model.pkl)|
| (Dialog UI entry) |     | - Sleep Duration (Hours/Minutes)   | <-------------------------- |                                   |
+-------------------+     | - Dynamic AI Color Badges          |    JSON { risk, stress }    | Returns ML Predictions            |
                          +------------------------------------+                             +-----------------------------------+
```

---

## 📁 Repository Structure

```text
Bangalore_major/
├── app/                                  # Android Application (Kotlin)
│   ├── src/main/java/com/example/healthmonitor/
│   │   ├── MainActivity.kt               # Entry point activity
│   │   ├── Navigation.kt                 # NavDisplay screen provider
│   │   ├── data/
│   │   │   ├── DataRepository.kt         # Health data repository & offline fallback
│   │   │   ├── HealthApiService.kt       # Retrofit REST API client for Flask server
│   │   │   └── HealthConnectManager.kt   # Integration with Android Health Connect API
│   │   ├── theme/
│   │   │   ├── Color.kt                  # Emerald Teal color palette
│   │   │   ├── Theme.kt                  # Material 3 light/dark color schemes
│   │   │   └── Type.kt                   # Typography definitions
│   │   └── ui/main/
│   │       ├── MainScreen.kt             # Jetpack Compose Dashboard UI & Cards
│   │       └── MainScreenViewModel.kt    # ViewModel managing UI state flows
│   └── build.gradle.kts                  # Android dependencies & Compose configuration
│
├── backend/                              # Machine Learning & Flask Backend (Python)
│   ├── app.py                            # Flask API server (/predict endpoint)
│   ├── train_model.py                    # Data preparation & Random Forest training script
│   ├── models/                           # Trained ML artifacts
│   │   ├── scaler.pkl                    # StandardScaler model
│   │   ├── risk_model.pkl                # Risk Classifier Random Forest model
│   │   └── stress_model.pkl              # Stress Classifier Random Forest model
│   ├── requirements.txt                  # Python dependencies
│   ├── vercel.json                       # Vercel serverless deployment configuration
│   └── Procfile                          # Gunicorn web server process file
│
├── build.gradle.kts                      # Root Gradle configuration
└── README.md                             # Documentation
```

---

## 🚀 Getting Started

### 1. Running the Flask ML Backend Locally

```bash
cd backend

# Activate virtual environment
.\venv\Scripts\Activate.ps1

# Start the Flask API server
python app.py
```
*The Flask server starts at `http://127.0.0.1:5000/`.*

### 2. Building & Running the Android App

Open the project in **Android Studio** or build from command line:

```powershell
# Build Debug APK
.\gradlew assembleDebug

# Run unit & UI tests
.\gradlew test
```

*The pre-compiled APK is generated at:*
`app/build/outputs/apk/debug/app-debug.apk`

---

## ☁️ Deployment

- **Live Cloud Backend (Vercel)**: `https://backend-lime-xi-18.vercel.app`
- **Predict API Endpoint**: `https://backend-lime-xi-18.vercel.app/predict`
- **Sample Request Payload**:
  ```json
  {
    "heartRate": 115,
    "systolicBp": 145,
    "diastolicBp": 95,
    "steps": 2500,
    "sleepHours": 4
  }
  ```
- **Sample Response**:
  ```json
  {
    "status": "success",
    "predictions": {
      "riskLevel": "High",
      "stressLevel": "High"
    }
  }
  ```

---

## 🛠 Tech Stack

- **Android Mobile App**: Kotlin, Jetpack Compose, Material 3, Coroutines, StateFlow, Retrofit 2, OkHttp 4, Gson, Android Health Connect.
- **Machine Learning & Backend**: Python 3.12, Flask, Scikit-Learn, Random Forest Classifier, Joblib, NumPy, Pandas, Vercel Serverless.
