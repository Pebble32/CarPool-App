package com.example.carpool.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.AuthApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.models.AuthenticationRequest;
import com.example.carpool.ui.activities.MainActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginFragment handles the user login functionality.
 * It provides UI elements for the user to input their email and password,
 * and buttons to either attempt login or navigate to the registration screen.
 * 
 * UI Elements:
 * - EditText editTextEmail: Input field for the user's email.
 * - EditText editTextPassword: Input field for the user's password.
 * - Button buttonLogin: Button to trigger the login attempt.
 * - Button buttonGoToRegister: Button to navigate to the registration screen.
 * 
 * Dependencies:
 * - AuthApi authApi: Interface for authentication API calls.
 * 
 * Methods:
 * - onCreateView: Initializes the UI elements and sets up click listeners for the buttons.
 * - attemptLogin: Validates the input fields and makes an API call to authenticate the user.
 * 
 * API Calls:
 * - authApi.authenticate: Sends the user's email and password to the server for authentication.
 * 
 * Navigation:
 * - On successful login, navigates to the RideOffersFragment.
 * - On clicking the registration button, navigates to the RegisterFragment.
 */
public class LoginFragment extends Fragment {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin, buttonGoToRegister;
    private AuthApi authApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        editTextEmail = view.findViewById(R.id.editTextEmailLogin);
        editTextPassword = view.findViewById(R.id.editTextPasswordLogin);
        buttonLogin = view.findViewById(R.id.buttonLogin);
        buttonGoToRegister = view.findViewById(R.id.buttonGoToRegister);

        authApi = RetrofitClient.getInstance().create(AuthApi.class);

        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonGoToRegister.setOnClickListener(v -> {
            // Navigate to the RegisterFragment if needed
            RegisterFragment registerFragment = new RegisterFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, registerFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showBottomNav(false);
    }

    private void attemptLogin() {
        // Collect user input from EditText fields
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate that both fields are filled
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an AuthenticationRequest object with the collected data
        AuthenticationRequest request = new AuthenticationRequest(email, password);
        
        // Make an API call to authenticate the user
        authApi.authenticate(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                    // Save the user's email in SharedPreferences and navigate to RideOffersFragment
                    if (getActivity() != null) {
                        SharedPreferences sp = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        sp.edit().putString("email", email).apply();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new RideOffersFragment())
                                .commit();
                    }
                } else {
                    Toast.makeText(getContext(), "Login failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
