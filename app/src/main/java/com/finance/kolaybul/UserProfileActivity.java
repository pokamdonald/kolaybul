package com.finance.kolaybul;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.finance.kolaybul.api.ApiClient;
import com.finance.kolaybul.api.ApiService;
import com.finance.kolaybul.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 100;

    private ImageView profilePicture, cameraIcon;
    private ApiService apiService;
    private TextView nameText, surnameText, emailText, studentNumberText, phoneText;
    private String authToken;
    private static final String BASE_URL = "http://192.168.1.108:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profilePicture = findViewById(R.id.profilePicture);
        cameraIcon = findViewById(R.id.cameraIcon);
        nameText = findViewById(R.id.name);
        surnameText = findViewById(R.id.surname);
        emailText = findViewById(R.id.email);
        studentNumberText = findViewById(R.id.studentNumber);
        phoneText = findViewById(R.id.phone);

        apiService = ApiClient.getApiService();

        ImageView backward = findViewById(R.id.backward);
        backward.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, Dashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // optional: avoid stacking
            startActivity(intent);
            finish(); // optional: close current activity
        });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.getIdToken(false).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    authToken = "Bearer " + task.getResult().getToken();
                    loadUserProfile();
                } else {
                    Toast.makeText(this, "Failed to get authentication token", Toast.LENGTH_SHORT).show();
                }
            });
        }

        cameraIcon.setOnClickListener(view -> openGallery());
        checkStoragePermission();
    }

    private void loadUserProfile() {
        apiService.getUserProfile(authToken).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    // Log all fields received from backend
                    Log.d("UserProfile", "User Info: " +
                            "\nName: " + user.getName() +
                            "\nSurname: " + user.getSurname() +
                            "\nEmail: " + user.getEmail() +
                            "\nStudent Number: " + user.getStudentNumber() +
                            "\nPhone: " + user.getPhone() +
                            "\nProfilePictureId: " + user.getProfilePictureId());

                    nameText.setText(user.getName() != null ? user.getName() : "N/A");
                    surnameText.setText(user.getSurname() != null ? user.getSurname() : "N/A");
                    emailText.setText(user.getEmail() != null ? user.getEmail() : "N/A");
                    studentNumberText.setText(user.getStudentNumber() != null ? user.getStudentNumber() : "N/A");

                    if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                        String phone = user.getPhone();
                        if (phone != null && !phone.startsWith("+90")) {
                            phone = "+90 " + phone;
                        }
                        phoneText.setText(phone);

                    } else {
                        Log.w("UserProfile", "Phone number is null or empty");
                        phoneText.setText("Not set");
                    }

                    // ✅ Load profile picture using Glide
                    if (user.getProfilePictureId() != null && !user.getProfilePictureId().isEmpty()) {
                        String imageUrl = BASE_URL + "/api/users/profile/picture/" + user.getProfilePictureId();
                        Glide.with(UserProfileActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.account)
                                .error(R.drawable.account)
                                .centerCrop()
                                .into(profilePicture);
                    } else {
                        profilePicture.setImageResource(R.drawable.account);
                    }

                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    Log.e("UserProfile", "Response error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(UserProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e("UserProfile", "API call failed: " + t.getMessage());
            }
        });
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                uploadProfilePicture(selectedImageUri);
            }
        }
    }

    private void uploadProfilePicture(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            if (bitmap == null) {
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
            byte[] compressedImage = byteArrayOutputStream.toByteArray();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), compressedImage);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "profile.jpg", requestFile);

            apiService.uploadProfilePicture(authToken, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(UserProfileActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        loadUserProfile(); // reload profile & image
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(UserProfileActivity.this, "Upload error", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            Toast.makeText(this, "Image processing failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }
}
