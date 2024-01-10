package com.example.mobile.model;

public class Grade {
    private String subject;
    private float grade;

    public Grade(String subject, float grade) {
        this.subject = subject;
        this.grade = grade;
    }

    // Getters
    public String getSubject() {
        return subject;
    }

    public float getGrade() {
        return grade;
    }
}
