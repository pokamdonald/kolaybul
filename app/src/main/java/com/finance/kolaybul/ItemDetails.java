package com.finance.kolaybul;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.finance.kolaybul.api.ApiClient;
import com.finance.kolaybul.api.ApiService;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetails extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 1;

    private ShapeableImageView imgItem;
    private EditText edtItemTitle, edtDescription, edtPhoneNumber, edtDatePosted, edtLocationFound, edtLocationToCollect;
    private Spinner spinnerCategory;
    private RadioGroup radioGroupLostFound;
    private Button btnSubmit;
    private Bitmap selectedImage;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_details);

        // Initialize views
        imgItem = findViewById(R.id.imgItem);
        edtItemTitle = findViewById(R.id.edtItemTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtDatePosted = findViewById(R.id.txtSelectedDate);
        edtLocationFound = findViewById(R.id.txtSelectedLocationFound);
        edtLocationToCollect = findViewById(R.id.txtSelectedLocationCollect);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        radioGroupLostFound = findViewById(R.id.radioGroupLostFound);
        btnSubmit = findViewById(R.id.btnSubmitItem);

        apiService = ApiClient.getApiService();

        // Setup category dropdown
        List<String> categories = Arrays.asList("Select Category", "Electronics", "Keys", "Dress", "Books", "Others");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Handle image selection
        imgItem.setOnClickListener(v -> pickImage());

        // Handle item submission
        btnSubmit.setOnClickListener(v -> submitItem());

        // Open calendar on click
        edtDatePosted.setFocusable(false);
        edtDatePosted.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedMonth = String.format("%02d", selectedMonth + 1);
                    String formattedDay = String.format("%02d", selectedDay);
                    String selectedDate = selectedYear + "-" + formattedMonth + "-" + formattedDay;
                    edtDatePosted.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void submitItem() {
        String title = edtItemTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String locationFound = edtLocationFound.getText().toString().trim();
        String locationCollect = edtLocationToCollect.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String date = edtDatePosted.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        int selectedRadioButtonId = radioGroupLostFound.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, "Please select Lost or Found.", Toast.LENGTH_SHORT).show();
            return;
        }
        String lostOrFound = ((RadioButton) findViewById(selectedRadioButtonId)).getText().toString();

        if (title.isEmpty() || description.isEmpty() || locationFound.isEmpty() || locationCollect.isEmpty() ||
                phoneNumber.isEmpty() || date.isEmpty() || category.equals("Select Category")) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part imagePart = null;
        if (selectedImage != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            RequestBody imageRequestBody = RequestBody.create(MultipartBody.FORM, imageBytes);
            imagePart = MultipartBody.Part.createFormData("imageFile", "item_image.jpg", imageRequestBody);
        }

        RequestBody namePart = RequestBody.create(MultipartBody.FORM, title);
        RequestBody statusPart = RequestBody.create(MultipartBody.FORM, lostOrFound);
        RequestBody descriptionPart = RequestBody.create(MultipartBody.FORM, description);
        RequestBody locationFoundPart = RequestBody.create(MultipartBody.FORM, locationFound);
        RequestBody locationCollectPart = RequestBody.create(MultipartBody.FORM, locationCollect);
        RequestBody categoryPart = RequestBody.create(MultipartBody.FORM, category);
        RequestBody phonePart = RequestBody.create(MultipartBody.FORM, phoneNumber);

        apiService.createItemWithImage(
                namePart, statusPart, descriptionPart, locationFoundPart, locationCollectPart, categoryPart, phonePart, imagePart
        ).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ItemDetails.this, "Item Submitted Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ItemDetails.this, "Error Submitting Item!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Toast.makeText(ItemDetails.this, "Failed to Submit Item!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(inputStream);
                imgItem.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
