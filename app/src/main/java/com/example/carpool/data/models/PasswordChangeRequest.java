package com.example.carpool.data.models;

import androidx.annotation.Size;

public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;

    public PasswordChangeRequest() {}

    public PasswordChangeRequest(@Size(min = 8) String oldPassword, @Size(min = 8) String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // getters
    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
