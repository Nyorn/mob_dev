package com.example.mobile.model;

import java.util.List;
import java.util.stream.Collectors;

public class Student {
    private String name;
    private List<Grade> grades;
    private float averageGrade; // Добавлено для хранения среднего балла

    // Конструктор с средним баллом
    public Student(String name, List<Grade> grades, float averageGrade) {
        this.name = name;
        this.grades = grades;
        this.averageGrade = averageGrade;
    }

    // Getters and setters

    public float calculateAverageGrade() {
        if (grades.isEmpty()) return 0;
        float sum = 0;
        for (Grade grade : grades) {
            sum += grade.getGrade();
        }
        return sum / grades.size();
    }

    public float calculateAverageGradeForSubject(String subject) {
        List<Grade> subjectGrades = grades.stream()
                .filter(grade -> grade.getSubject().equals(subject))
                .collect(Collectors.toList());
        if (subjectGrades.isEmpty()) return 0;
        float sum = 0;
        for (Grade grade : subjectGrades) {
            sum += grade.getGrade();
        }
        return sum / subjectGrades.size();
    }

    // Метод для получения среднего балла
    public float getAverageGrade() {
        return averageGrade;
    }
    public String getName() {
        return name;
    }
    public List<Grade> getGrades() {
        return grades;
    }
    // Метод для установки среднего балла
    public void setAverageGrade(float averageGrade) {
        this.averageGrade = averageGrade;
    }
}
