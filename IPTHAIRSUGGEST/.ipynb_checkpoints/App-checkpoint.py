from flask import Flask, request, jsonify
import joblib
import numpy as np
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # Allow cross-origin requests

try:
    model = joblib.load('hairstyle_model.pkl')  # Ensure this file exists
    label_encoders = joblib.load('label_encoders.pkl')  # Ensure this file exists
except Exception as e:
    print(f"Error loading model or label encoders: {e}")

def make_prediction(data):
    try:
        face_shape = data.get('face_shape')
        gender = data.get('gender')
        age_group = data.get('age_group')

        # Validate that the provided values exist in the label encoders
        if face_shape not in label_encoders['FaceShape'].classes_:
            return {'error': 'Invalid FaceShape value. Must be one of ' + str(label_encoders['FaceShape'].classes_)}, 400
        if gender not in label_encoders['Gender'].classes_:
            return {'error': 'Invalid Gender value. Must be one of ' + str(label_encoders['Gender'].classes_)}, 400
        if age_group not in label_encoders['AgeGroup'].classes_:
            return {'error': 'Invalid AgeGroup value. Must be one of ' + str(label_encoders['AgeGroup'].classes_)}, 400

        # Encode the inputs
        face_shape_encoded = label_encoders['FaceShape'].transform([face_shape])[0]
        gender_encoded = label_encoders['Gender'].transform([gender])[0]
        age_group_encoded = label_encoders['AgeGroup'].transform([age_group])[0]

        # Prepare the features for prediction
        features = np.array([[face_shape_encoded, gender_encoded, age_group_encoded]])
        prediction = model.predict(features)

        # Return the predicted hairstyle
        return {'RecommendedHairstyle': prediction[0]}, 200

    except KeyError as ke:
        return {'error': f'Missing key: {str(ke)}'}, 400
    except ValueError as ve:
        return {'error': f'Invalid value: {str(ve)}'}, 400
    except Exception as e:
        return {'error': str(e)}, 500

@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json()
    print("Received data:", data)  # Log received data for debugging

    # Check if data is provided
    if not data:
        return jsonify({'error': 'No data provided'}), 400

    # Call the prediction function and return the result
    result, status_code = make_prediction(data)
    return jsonify(result), status_code

if __name__ == '__main__':
    # Update the host IP to listen on the correct address
    app.run(debug=True, host='0.0.0.0', port=5000)
