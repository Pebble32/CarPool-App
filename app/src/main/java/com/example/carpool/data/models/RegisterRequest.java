package com.example.carpool.data.models;

public class RegisterRequest {
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String profilePicture;

    public RegisterRequest(String email, String password, String firstname, String lastname, String phoneNumber, String profilePicture) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
    }

    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getProfilePicture() {
        return profilePicture;
    }
}
