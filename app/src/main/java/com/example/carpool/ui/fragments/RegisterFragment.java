package com.example.mycarpoolapp.ui.fragments;

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
import com.example.carpool.data.models.RegisterRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText editTextEmailRegister;
    private EditText editTextPasswordRegister;
    private EditText editTextFullNameRegister;
    private Button buttonRegisterConfirm;

    private AuthApi authApi;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        editTextEmailRegister = view.findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = view.findViewById(R.id.editTextPasswordRegister);
        editTextFullNameRegister = view.findViewById(R.id.editTextFullNameRegister);
        buttonRegisterConfirm = view.findViewById(R.id.buttonRegisterConfirm);

        authApi = RetrofitClient.getInstance().create(AuthApi.class);

        buttonRegisterConfirm.setOnClickListener(v -> attemptRegister());

        return view;
    }

    private void attemptRegister() {
        String email = editTextEmailRegister.getText().toString().trim();
        String password = editTextPasswordRegister.getText().toString().trim();
        String fullName = editTextFullNameRegister.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(email, password, fullName);
        authApi.register(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> response
            ) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();

                    // After register, navigate back to LoginFragment (or HomeFragment if you want)
                    // e.g. with NavController:
                    // NavHostFragment.findNavController(RegisterFragment.this)
                    //     .popBackStack(); // goes back to login
                } else {
                    Toast.makeText(getContext(), "Registration failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
