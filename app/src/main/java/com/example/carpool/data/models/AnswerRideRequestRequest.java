package com.example.carpool.data.models;

import com.google.gson.annotations.SerializedName;

public class AnswerRideRequestRequest {
    
    @SerializedName("rideRequestId")
    private Long rideRequestId;

    @SerializedName("answerStatus")
    private String answerStatus;


    public AnswerRideRequestRequest(Long rideRequestId, String answerStatus) {
        this.rideRequestId = rideRequestId;
        this.answerStatus = answerStatus;
    }

    public Long getRideRequestId() {
        return rideRequestId;
    }

    public void setRideRequestId(Long rideRequestId) {
        this.rideRequestId = rideRequestId;
    }

    public String getAnswerStatus() {
        return answerStatus;
    }

    public void setAnswerStatus(String answerStatus) {
        this.answerStatus = answerStatus;
    }

    public static class AnswerStatus{
        public static final String ACCEPTED = "ACCEPTED";
        public static final String REJECTED = "REJECTED";
    }
}
