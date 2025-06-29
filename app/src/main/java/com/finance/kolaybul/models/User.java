package com.finance.kolaybul.models;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("name")
    private String name;

    @SerializedName("surname")
    private String surname;

    @SerializedName("studentNumber")
    private String studentNumber;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("id")
    private String id;

    @SerializedName("profilePictureId")
    private String profilePictureId;

    private static final String BASE_URL = "http://192.168.1.108:8080";

    // ✅ Constructor matches order: name, surname, studentNumber, phone, email, password
    public User(String name, String surname, String studentNumber, String phone, String email, String password) {
        this.name = name;
        this.surname = surname;
        this.studentNumber = studentNumber;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    public User() {}

    // Getters and Setters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getStudentNumber() { return studentNumber; }
    public String getPhone() { return phone; }
    public String getProfilePictureId() { return profilePictureId; }

    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProfilePictureId(String profilePictureId) { this.profilePictureId = profilePictureId; }

    public String getProfilePictureUrl() {
        if (profilePictureId != null && !profilePictureId.isEmpty()) {
            return BASE_URL + "/api/users/profile/picture/" + profilePictureId;
        } else {
            return BASE_URL + "/images/default_profile_picture.jpg";
        }
    }
}
