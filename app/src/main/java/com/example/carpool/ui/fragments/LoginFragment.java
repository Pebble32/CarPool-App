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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private void attemptLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthenticationRequest request = new AuthenticationRequest(email, password);
        authApi.authenticate(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                    // Navigate to RideOffersFragment (the main page)
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
