from flask import Flask, request, jsonify
import joblib
import numpy as np
import os

app = Flask(__name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SCALER_PATH = os.path.join(BASE_DIR, 'models', 'scaler.pkl')
RISK_MODEL_PATH = os.path.join(BASE_DIR, 'models', 'risk_model.pkl')
STRESS_MODEL_PATH = os.path.join(BASE_DIR, 'models', 'stress_model.pkl')

scaler = None
risk_model = None
stress_model = None

if os.path.exists(SCALER_PATH) and os.path.exists(RISK_MODEL_PATH):
    scaler = joblib.load(SCALER_PATH)
    risk_model = joblib.load(RISK_MODEL_PATH)
    stress_model = joblib.load(STRESS_MODEL_PATH)
    print("ML Models loaded successfully!")

@app.route('/predict', methods=['POST'])
def predict_health():
    """
    Endpoint to predict health risk and stress level using trained Random Forest Models.
    Expects JSON payload with: heartRate, systolicBp, diastolicBp, steps, sleepHours.
    """
    try:
        data = request.get_json()
        
        heart_rate = data.get('heartRate', 72)
        systolic_bp = data.get('systolicBp', 120)
        diastolic_bp = data.get('diastolicBp', 80)
        steps = data.get('steps', 5000)
        sleep_hours = data.get('sleepHours', 7)
        
        # Default predictions if model not loaded
        risk_level = "Unknown"
        stress_level = "Unknown"
        
        if scaler and risk_model and stress_model:
            # Prepare feature array matching train_model.py order
            features = np.array([[heart_rate, systolic_bp, diastolic_bp, steps, sleep_hours]])
            
            # Normalize
            features_scaled = scaler.transform(features)
            
            # Predict
            risk_level = risk_model.predict(features_scaled)[0]
            stress_level = stress_model.predict(features_scaled)[0]
        else:
            # Fallback mock logic if models aren't trained yet
            if heart_rate > 100 or systolic_bp > 140 or diastolic_bp > 90:
                risk_level = "High"
                stress_level = "High"
            elif sleep_hours < 5 or steps < 3000:
                risk_level = "Medium"
                stress_level = "Moderate"
            else:
                risk_level = "Low"
                stress_level = "Normal"
            
        return jsonify({
            'status': 'success',
            'predictions': {
                'riskLevel': risk_level,
                'stressLevel': stress_level
            }
        })
        
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 400

@app.route('/', methods=['GET'])
def health_check():
    return "Health Monitor ML Backend is running!"

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
