package com.finance.kolaybul;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finance.kolaybul.api.ApiClient;
import com.finance.kolaybul.api.ApiService;
import com.finance.kolaybul.models.User;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Signin extends AppCompatActivity {

    private EditText editTextFirstName, editTextSurname, editTextStudentNumber, editTextEmail,
            editTextPassword, editTextConfirmPassword, editTextPhone;
    private CheckBox checkboxPolicies;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        apiService = ApiClient.getClient().create(ApiService.class);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextStudentNumber = findViewById(R.id.editTextStudentNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        checkboxPolicies = findViewById(R.id.checkboxPolicies);

        // ✅ Enforce max 9 digits on student number
        editTextStudentNumber.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(9)
        });

        findViewById(R.id.buttonConfirm).setOnClickListener(v -> createUser());
    }

    private void createUser() {
        String firstName = editTextFirstName.getText().toString().trim();
        String surname = editTextSurname.getText().toString().trim();
        String studentNumber = editTextStudentNumber.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        // ✅ Validations
        if (firstName.isEmpty() || surname.isEmpty() || studentNumber.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            Toast.makeText(Signin.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(Signin.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(Signin.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkboxPolicies.isChecked()) {
            Toast.makeText(Signin.this, "Please accept the policies", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!studentNumber.matches("^[0-9]{9}$")) {
            Toast.makeText(this, "Student number must be exactly 9 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("^[25][0-9]{9}$")) {
            Toast.makeText(this, "Phone number must start with 2 or 5 and be 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optional: Prefix +90
        if (!phone.startsWith("+90")) {
            phone = "+90" + phone;
        }

        User user = new User(firstName, surname, studentNumber, phone, email, password);

        apiService.signUp(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Signin.this, "User Created Successfully. Please log in.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Signin.this, Login.class));
                    finish();
                } else {
                    Toast.makeText(Signin.this, "Sign Up Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(Signin.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
