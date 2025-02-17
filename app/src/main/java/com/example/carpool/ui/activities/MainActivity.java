package com.example.carpool.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.carpool.R;
import com.example.carpool.ui.fragments.LoginFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Loads the fragment container

        // If first creation, load LoginFragment into the container
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
    }
}
