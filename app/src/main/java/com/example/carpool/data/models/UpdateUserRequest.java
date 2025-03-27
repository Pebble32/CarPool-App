package com.example.carpool.data.models;

public class UpdateUserRequest {
    private String firstname;
    private String lastname;
    private String phoneNumber;

    public UpdateUserRequest(String firstname, String lastname, String phoneNumber) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.phoneNumber = phoneNumber;
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
