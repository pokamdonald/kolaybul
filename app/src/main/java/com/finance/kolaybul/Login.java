package com.finance.kolaybul;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finance.kolaybul.api.ApiClient;
import com.finance.kolaybul.api.ApiService;
import com.finance.kolaybul.LoginRequest;
import com.finance.kolaybul.LoginResponse;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;
    private TextView signUpText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String savedToken = sharedPreferences.getString("auth_token", null);

        if (savedToken != null) {
            // ✅ User is already logged in, navigate to Dashboard
            navigateToDashboard();
            return; // Stop loading the login page
        }

        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        loginButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signUpText);

        loginButton.setOnClickListener(v -> loginUser());
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Signin.class);
            startActivity(intent);
        });

        passwordInputLayout.setEndIconOnClickListener(v -> togglePasswordVisibility());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                                if (tokenTask.isSuccessful()) {
                                    String idToken = tokenTask.getResult().getToken();
                                    sendTokenToBackend(idToken);
                                } else {
                                    Toast.makeText(Login.this, "Failed to get ID token", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(Login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendTokenToBackend(String idToken) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(idToken);

        Call<LoginResponse> call = apiService.loginUser(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // ✅ Save Token Persistently
                    SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("auth_token", loginResponse.getToken()); // Save token
                    editor.putString("user_id", loginResponse.getUserId()); // Save user ID
                    editor.apply(); // Commit the changes

                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    Toast.makeText(Login.this, "Invalid login", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(Login.this, "API call failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(Login.this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void togglePasswordVisibility() {
        if (passwordEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordInputLayout.setEndIconDrawable(R.drawable.ic_visibility_off);
        } else {
            passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordInputLayout.setEndIconDrawable(R.drawable.ic_visibility);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // ✅ Ensure Firebase does not sign out the user automatically
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // If user exists, navigate directly to the dashboard
            navigateToDashboard();
        }
    }
}
