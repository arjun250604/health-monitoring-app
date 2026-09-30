import os
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, classification_report
import joblib

def generate_synthetic_data(num_samples=1000):
    """
    Generates synthetic health data mimicking the UCI Human Activity/Health dataset.
    In a real scenario (Step 2), you would load the CSV here using pd.read_csv().
    """
    np.random.seed(42)
    
    # Features (Step 1: Select Parameters)
    heart_rate = np.random.normal(75, 15, num_samples)
    systolic_bp = np.random.normal(120, 20, num_samples)
    diastolic_bp = np.random.normal(80, 15, num_samples)
    steps = np.random.normal(6000, 3000, num_samples)
    sleep_hours = np.random.normal(7, 1.5, num_samples)
    
    # Calculate simple rules to generate labels for synthetic data
    risk_level = []
    stress_level = []
    
    for i in range(num_samples):
        # Stress Logic
        if heart_rate[i] > 100 or systolic_bp[i] > 140:
            stress = "High"
        elif heart_rate[i] > 85 or systolic_bp[i] > 130:
            stress = "Moderate"
        else:
            stress = "Normal"
        stress_level.append(stress)
        
        # Risk Logic
        if stress == "High" or sleep_hours[i] < 5 or steps[i] < 3000:
            risk = "High"
        elif stress == "Moderate" or sleep_hours[i] < 6 or steps[i] < 5000:
            risk = "Medium"
        else:
            risk = "Low"
        risk_level.append(risk)
        
    df = pd.DataFrame({
        'heartRate': heart_rate,
        'systolicBp': systolic_bp,
        'diastolicBp': diastolic_bp,
        'steps': steps,
        'sleepHours': sleep_hours,
        'riskLevel': risk_level,
        'stressLevel': stress_level
    })
    
    return df

def clean_and_normalize(df):
    """Step 3: Clean & Normalize Data"""
    # Remove any NaNs (simulated)
    df = df.dropna()
    return df

def train_and_evaluate():
    print("Step 2: Loading Data...")
    df = generate_synthetic_data(2000)
    
    print("Step 3: Cleaning Data...")
    df = clean_and_normalize(df)
    
    print("Step 4: Feature Selection...")
    # Select our most predictive variables
    X = df[['heartRate', 'systolicBp', 'diastolicBp', 'steps', 'sleepHours']]
    
    # Target 1: Risk Level
    y_risk = df['riskLevel']
    # Target 2: Stress Level
    y_stress = df['stressLevel']
    
    # Split for Risk Model
    X_train, X_test, y_train, y_test = train_test_split(X, y_risk, test_size=0.2, random_state=42)
    
    # Normalize
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)
    
    print("Step 5 & 6: Training Random Forest Models...")
    # Train Risk Model
    risk_model = RandomForestClassifier(n_estimators=100, random_state=42)
    risk_model.fit(X_train_scaled, y_train)
    
    # Train Stress Model
    stress_model = RandomForestClassifier(n_estimators=100, random_state=42)
    # Using same split for simplicity on synthetic data
    stress_model.fit(X_train_scaled, y_stress[:len(y_train)]) 
    
    print("Step 7: Validating Accuracy...")
    risk_preds = risk_model.predict(X_test_scaled)
    print(f"Risk Model Accuracy: {accuracy_score(y_test, risk_preds) * 100:.2f}%")
    
    # Save the models and scaler
    print("Saving Models to disk for Backend Integration (Step 8)...")
    os.makedirs('models', exist_ok=True)
    joblib.dump(scaler, 'models/scaler.pkl')
    joblib.dump(risk_model, 'models/risk_model.pkl')
    joblib.dump(stress_model, 'models/stress_model.pkl')
    print("Models saved in backend/models/ directory!")

if __name__ == "__main__":
    train_and_evaluate()
