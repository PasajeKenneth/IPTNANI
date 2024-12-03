package com.example.hairsuggestionipt;

import com.google.gson.annotations.SerializedName;

public class HairstyleRequest {
    private String face_shape;
    private String gender;
    private String age_group;

    public HairstyleRequest(String faceShape, String gender, String ageGroup) {
        this.face_shape = faceShape;
        this.gender = gender;
        this.age_group = ageGroup;
    }
}
