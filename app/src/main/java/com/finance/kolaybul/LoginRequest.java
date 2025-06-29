package com.finance.kolaybul;

public class LoginRequest {
    private String idToken;  // Firebase Authentication Token

    // Constructor
    public LoginRequest(String idToken) {
        this.idToken = idToken;
    }

    // Getter and Setter
    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
