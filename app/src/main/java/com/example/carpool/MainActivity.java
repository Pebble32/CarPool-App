package com.example.carpool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carpool.activities.RideOfferActivity;
import com.google.android.material.snackbar.Snackbar;
import com.example.carpool.databinding.ActivityMainBinding;

/**
 * MainActivity is the entry point of the CarPool app. It handles user login functionality.
 * It uses View Binding to interact with the UI elements.
 * 
 * Constants:
 * - TEST_EMAIL: A test email for login validation.
 * - TEST_PASSWORD: A test password for login validation.
 * 
 * Methods:
 * - onCreate(Bundle savedInstanceState): Initializes the activity, sets up the view binding, and sets the login button click listener.
 * - validateInput(String email, String password): Validates the email and password input fields.
 * - attemptLogin(String email, String password): Attempts to log in with the provided email and password. If successful, navigates to RideOfferActivity. Otherwise, shows an error message.
 */
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