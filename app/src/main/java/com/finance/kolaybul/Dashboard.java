package com.finance.kolaybul;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.finance.kolaybul.api.ApiClient;
import com.finance.kolaybul.api.ApiService;
import com.finance.kolaybul.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> itemList;
    private ImageView btnProfile, btnFilter;
    private FloatingActionButton btnAddItem, btnLogout;
    private LinearLayout filterPanel;
    private View filterOverlay;
    private Spinner spinnerStatus;
    private Button btnSelectDate, btnApplyFilter;
    private TextView txtSelectedDate;
    private ApiService apiService;
    private String selectedDate = "", selectedStatus = "";
    private boolean isFilterVisible = false;
    private static final String BASE_URL = "http://10.70.125.160:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        recyclerView = findViewById(R.id.recyclerViewItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnProfile = findViewById(R.id.btnProfile);
        btnFilter = findViewById(R.id.btnFilter);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnLogout = findViewById(R.id.btnLogout);
        filterPanel = findViewById(R.id.filterPanel);
        filterOverlay = findViewById(R.id.filterOverlay);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        txtSelectedDate = findViewById(R.id.txtSelectedDate);
        apiService = ApiClient.getApiService();

        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapterSpinner);

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, UserProfileActivity.class);
            startActivity(intent);
        });

        btnFilter.setOnClickListener(v -> toggleFilterPanel());
        filterOverlay.setOnClickListener(v -> hideFilterPanel());

        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, ItemDetails.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("auth_token");
            editor.remove("user_id");
            editor.apply();

            Intent intent = new Intent(Dashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnApplyFilter.setOnClickListener(v -> applyFilters());

        itemList = new ArrayList<>();
        adapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        fetchAllItems();
        fetchUserProfilePicture();
    }

    private void fetchUserProfilePicture() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.getIdToken(false).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = "Bearer " + task.getResult().getToken();
                    apiService.getUserProfile(token).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String pictureId = response.body().getProfilePictureId();
                                if (pictureId != null && !pictureId.isEmpty()) {
                                    String imageUrl = BASE_URL + "/api/users/profile/picture/" + pictureId;
                                    Glide.with(Dashboard.this)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.account)
                                            .error(R.drawable.account)
                                            .circleCrop()
                                            .into(btnProfile);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(Dashboard.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void fetchAllItems() {
        Call<List<Item>> call = apiService.getAllItems();
        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();
                    itemList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(Dashboard.this, "Failed to load items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Toast.makeText(Dashboard.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        selectedStatus = spinnerStatus.getSelectedItem().toString();
        if (selectedStatus.equals("Select Status")) selectedStatus = "";

        if (selectedStatus.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select both status and date!", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<Item>> call = apiService.getItemsByStatusAndDate(selectedStatus, selectedDate);
        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();
                    itemList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (response.body().isEmpty()) {
                        Toast.makeText(Dashboard.this, "No matching items found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Dashboard.this, "Filter error: No items found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Toast.makeText(Dashboard.this, "API Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Dashboard.this,
                (view, year, month, dayOfMonth) -> {
                    String formattedMonth = String.format("%02d", month + 1);
                    String formattedDay = String.format("%02d", dayOfMonth);
                    selectedDate = year + "-" + formattedMonth + "-" + formattedDay;
                    txtSelectedDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void toggleFilterPanel() {
        if (isFilterVisible) {
            hideFilterPanel();
        } else {
            filterOverlay.setVisibility(View.VISIBLE);
            filterPanel.setVisibility(View.VISIBLE);
            filterPanel.animate().translationY(0).setDuration(300);
        }
        isFilterVisible = !isFilterVisible;
    }

    private void hideFilterPanel() {
        filterOverlay.setVisibility(View.GONE);
        filterPanel.animate().translationY(-filterPanel.getHeight()).setDuration(300).withEndAction(() -> {
            filterPanel.setVisibility(View.GONE);
        });
        isFilterVisible = false;
    }
}
