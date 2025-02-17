package com.example.carpool.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.AuthApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.model.RegisterRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName, editTextPhone, editTextProfilePicture;
    private Button buttonRegister;
    private AuthApi authApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextProfilePicture = view.findViewById(R.id.editTextProfilePicture);
        buttonRegister = view.findViewById(R.id.buttonRegister);
        authApi = RetrofitClient.getInstance().create(AuthApi.class);

        buttonRegister.setOnClickListener(v -> registerUser());
        return view;
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String firstname = editTextFirstName.getText().toString().trim();
        String lastname = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhone.getText().toString().trim();
        String profilePicture = editTextProfilePicture.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(firstname)
                || TextUtils.isEmpty(lastname) || TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(email, password, firstname, lastname, phoneNumber, profilePicture);
        authApi.register(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                    // Navigate back to the LoginFragment by popping the back stack
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
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
