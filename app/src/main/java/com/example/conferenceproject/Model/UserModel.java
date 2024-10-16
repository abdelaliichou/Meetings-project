package com.example.conferenceproject.Model;

import com.google.firebase.database.PropertyName;

public class UserModel {
    private String email ;
    private String phonenumber ;
    String name ;
    @PropertyName("FCM_Token")
    String FCM_Token ;
    String image ;

    public UserModel(String name ,String email, String phonenumber , String FCM_Token , String image) {
        this.name = name ;
        this.email = email;
        this.phonenumber = phonenumber;
        this.FCM_Token = FCM_Token ;
        this.image = image ;
    }

    public UserModel() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    @PropertyName("FCM_Token")
    public String getToken() {
        return FCM_Token;
    }

    public void setToken(String token) {
        this.FCM_Token = token;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
