package com.example.carpool.data.models;

public class UserResponse {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;


    public UserResponse(String email, String password, String firstName, String phone, String lastName) {
        this.email = email;
        this.password = password;
        this.phoneNumber = phone;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
