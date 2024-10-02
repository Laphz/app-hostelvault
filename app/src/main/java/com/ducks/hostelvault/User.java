package com.ducks.hostelvault;

public class User {
    private String name;
    private String email;
    private String mobileNumber;
    private String hostelId;
    private String status;


    public User(String name, String email, String mobileNumber, String hostelId) {
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.hostelId = hostelId;
        this.status = "OUT";
    }

    // Constructor with status as parameter
    public User(String name, String email, String mobileNumber, String hostelId, String status) {
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.hostelId = hostelId;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getHostelId() {
        return hostelId;
    }

    public String getStatus() {
        return status;
    }

    // Setter for status
    public void setStatus(String status) {
        this.status = status;
    }

}

