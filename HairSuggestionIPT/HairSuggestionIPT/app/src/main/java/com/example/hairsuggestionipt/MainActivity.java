package com.example.hairsuggestionipt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;  // Add this import

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerFaceShape, spinnerAgeGroup;
    private RadioGroup radioGroupGender;
    private Button buttonSubmit;
    private TextView textViewResult;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        spinnerFaceShape = findViewById(R.id.spinnerFaceShape);
        spinnerAgeGroup = findViewById(R.id.spinnerAgeGroup);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewResult = findViewById(R.id.textViewResult);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Retrofit with logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.33.107:5000/") // Your base URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Set up the Spinners
        setupSpinners();

        // Submit button onClick listener
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitUserData();
            }
        });
    }

    private void setupSpinners() {
        // Face Shape Spinner
        ArrayAdapter<CharSequence> faceShapeAdapter = ArrayAdapter.createFromResource(this,
                R.array.face_shapes, android.R.layout.simple_spinner_item);
        faceShapeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFaceShape.setAdapter(faceShapeAdapter);

        // Age Group Spinner
        ArrayAdapter<CharSequence> ageGroupAdapter = ArrayAdapter.createFromResource(this,
                R.array.age_groups, android.R.layout.simple_spinner_item);
        ageGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeGroup.setAdapter(ageGroupAdapter);
    }

    private void submitUserData() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        String faceShape = spinnerFaceShape.getSelectedItem().toString();
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();

        // Check if gender is selected
        if (selectedGenderId == -1) {
            progressBar.setVisibility(View.GONE);
            textViewResult.setText("Please select a gender.");
            return;
        }

        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String gender = selectedGenderButton.getText().toString(); // Keep the case as is
        String ageGroup = spinnerAgeGroup.getSelectedItem().toString();

        // Log the request details
        Log.d("Request", "face_shape: " + faceShape + ", gender: " + gender + ", age_group: " + ageGroup);

        HairstyleRequest request = new HairstyleRequest(faceShape, gender, ageGroup);

        apiService.getHairstyleSuggestion(request).enqueue(new Callback<HairstyleResponse>() {
            @Override
            public void onResponse(Call<HairstyleResponse> call, retrofit2.Response<HairstyleResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String recommendedHairstyle = response.body().getRecommendedHairstyle();
                        textViewResult.setText("Recommended Hairstyle: " + recommendedHairstyle);
                    } else {
                        textViewResult.setText("Recommended Hairstyle: null");
                        Log.e("Response", "Response body is null");
                    }
                } else {
                    Log.e("ResponseError", "Response code: " + response.code() + " - " + response.message());

                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string(); // Get the error body
                            Log.d("ResponseBody", "Error Body: " + errorBody);
                            textViewResult.setText("Error: " + errorBody);
                        } catch (IOException e) {
                            Log.e("Error", "Failed to parse error body", e);
                            textViewResult.setText("Error: Unable to parse error response.");
                        }
                    } else {
                        textViewResult.setText("Error in response: " + response.code() + " - " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<HairstyleResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                textViewResult.setText("Error: " + t.getMessage());
                Log.e("Error", "Request failed", t);
            }
        });
    }
}
