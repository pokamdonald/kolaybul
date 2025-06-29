package com.finance.kolaybul.api;

import com.finance.kolaybul.Item;
import com.finance.kolaybul.LoginRequest;
import com.finance.kolaybul.LoginResponse;
import com.finance.kolaybul.models.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    // 🔐 Sign up (Create new user)
    @POST("/api/users/signup")
    Call<User> signUp(@Body User user);

    // 🔐 Login user (returns Firebase custom token)
    @POST("/api/users/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // ✅ Get authenticated user profile (based on Firebase token)
    @GET("/api/users/profile")
    Call<User> getUserProfile(@Header("Authorization") String token);

    // ✅ Update authenticated user's profile (name, phone, etc.)
    @PUT("/api/users/profile")
    Call<User> updateUserProfile(@Header("Authorization") String token, @Body User user);

    // ✅ Upload profile picture
    @Multipart
    @POST("/api/users/profile/picture")
    Call<ResponseBody> uploadProfilePicture(
            @Header("Authorization") String token,
            @Part MultipartBody.Part file
    );

    // ✅ Get profile picture by its file ID
    @GET("/api/users/profile/picture/{id}")
    Call<ResponseBody> getProfilePicture(@Path("id") String fileId);

    // ✅ GET all items
    @GET("/api/items")
    Call<List<Item>> getAllItems();

    // ✅ GET item by ID
    @GET("/api/items/{id}")
    Call<Item> getItemById(@Path("id") String itemId);

    // ✅ GET filtered items by STATUS and DATE
    @GET("/api/items/search")
    Call<List<Item>> getItemsByStatusAndDate(
            @Query("status") String status,
            @Query("date") String date // Format: YYYY-MM-DD
    );

    // ✅ CREATE item with image upload
    @Multipart
    @POST("/api/items/add")
    Call<Item> createItemWithImage(
            @Part("name") RequestBody name,
            @Part("status") RequestBody status,
            @Part("description") RequestBody description,
            @Part("locationFound") RequestBody locationFound,
            @Part("locationToCollect") RequestBody locationToCollect,
            @Part("category") RequestBody category,
            @Part("phone") RequestBody phone,
            @Part MultipartBody.Part imageFile
    );

    // ✅ UPDATE item
    @PUT("/api/items/{id}")
    Call<Item> updateItem(@Path("id") String itemId, @Body Item item);

    // ✅ DELETE item
    @DELETE("/api/items/{id}")
    Call<Void> deleteItem(@Path("id") String itemId);
}
