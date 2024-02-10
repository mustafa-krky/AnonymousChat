package com.kmobile.anonymouschat.model;

public class User {
    private String userID;
    private String userName;
    private String email;
    private String userImage;
    private boolean userOnline;

    public User(){

    }

    public User(String userID, String userName, String email, String userImage, boolean userOnline){
        this.userID = userID;
        this.userName = userName;
        this.email = email;
        this.userImage = userImage;
        this.userOnline = userOnline;
    }

    public boolean isUserOnline() {
        return userOnline;
    }

    public void setUserOnline(boolean userOnline) {
        this.userOnline = userOnline;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}
