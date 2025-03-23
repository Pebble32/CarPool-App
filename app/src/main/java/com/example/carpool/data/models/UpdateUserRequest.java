package com.example.carpool.data.models;

public class UpdateUserRequest {
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String phoneNumber;

    public UpdateUserRequest(String email, String password, String firstname, String lastname, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phoneNumber = phoneNumber;
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
}
