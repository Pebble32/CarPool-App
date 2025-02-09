package com.example.carpool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carpool.activities.RideOfferActivity;
import com.google.android.material.snackbar.Snackbar;
import com.example.carpool.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailInput.getText().toString();
                String password = binding.passwordInput.getText().toString();

                if (validateInput(email, password)) {
                    attemptLogin(email, password);
                }
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            binding.emailInput.setError("Email cannot be empty");
            return false;
        }
        if (password.isEmpty()) {
            binding.passwordInput.setError("Password cannot be empty");
            return false;
        }
        return true;
    }

    private void attemptLogin(String email, String password) {
        if (email.equals(TEST_EMAIL) && password.equals(TEST_PASSWORD)) {
            // Successful login
            Intent intent = new Intent(MainActivity.this, RideOfferActivity.class);
            startActivity(intent);
            finish(); // Close login activity
        } else {
            // Failed login
            Snackbar.make(binding.getRoot(),
                    "Invalid credentials",
                    Snackbar.LENGTH_SHORT).show();
        }
    }
}