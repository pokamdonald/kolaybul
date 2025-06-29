package com.finance.kolaybul;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Delay & decide where to go
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // User is logged in
                startActivity(new Intent(MainActivity.this, Dashboard.class));
            } else {
                // Not logged in
                startActivity(new Intent(MainActivity.this, Login.class));
            }
            finish(); // close splash screen
        }, SPLASH_DELAY);
    }
}
