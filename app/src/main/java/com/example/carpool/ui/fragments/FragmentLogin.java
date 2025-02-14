// ui/fragments/LoginFragment.java
package com.example.carpool.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
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

public class FragmentLogin extends Fragment {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin, buttonGoToRegister;

    private AuthApi authApi;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize UI elements
        editTextEmail = view.findViewById(R.id.editTextEmailLogin);
        editTextPassword = view.findViewById(R.id.editTextPasswordLogin);
        buttonLogin = view.findViewById(R.id.buttonLogin);
        buttonGoToRegister = view.findViewById(R.id.buttonGoToRegister);

        // Create the API client
        authApi = RetrofitClient.getInstance().create(AuthApi.class);

        // Set onClick listeners
        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonGoToRegister.setOnClickListener(v -> {
            // Navigate to RegisterFragment (using NavController or fragment transaction)
            // For example, if using NavController:
            // NavHostFragment.findNavController(LoginFragment.this)
            //     .navigate(R.id.action_loginFragment_to_registerFragment);

            // If you're doing manual transactions, you'd do something like:
            // getActivity().getSupportFragmentManager().beginTransaction()
            //     .replace(R.id.my_container, new RegisterFragment())
            //     .addToBackStack(null)
            //     .commit();
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
            public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> response
            ) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                    // Navigate to HomeFragment or Main screen
                    // Example with NavController:
                    // NavHostFragment.findNavController(LoginFragment.this)
                    //     .navigate(R.id.action_loginFragment_to_homeFragment);
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
