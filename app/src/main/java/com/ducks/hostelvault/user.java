package com.ducks.hostelvault;

public class user {
    private String name;
    private String email;
    private String mobileNumber;
    private String roomNumber;
    private String hostelName;
    private String status = "OUT" ;


    public user(String name, String email,String mobileNumber) {
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
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

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getHostelName() {
        return hostelName;
    }

    public String getStatus() {
        return status;
    }
}

