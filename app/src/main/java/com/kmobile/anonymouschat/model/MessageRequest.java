package com.kmobile.anonymouschat.model;

public class MessageRequest {
    private String kanalID;
    private String userID;
    private String userName;
    private String userProfileImage;

    public MessageRequest() {
    }

    public MessageRequest(String kanalID, String userID, String userName, String userProfileImage) {
        this.kanalID = kanalID;
        this.userID = userID;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
    }

    public String getKanalID() {
        return kanalID;
    }

    public void setKanalID(String kanalID) {
        this.kanalID = kanalID;
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

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }
}
