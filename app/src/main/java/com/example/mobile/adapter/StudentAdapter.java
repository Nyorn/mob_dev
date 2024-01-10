package com.example.mobile.adapter;
import com.example.mobile.model.Student;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.mobile.R;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;  // Добавьте этот импорт
import com.example.mobile.model.Grade; // Добавьте этот импорт

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private List<Student> studentList;

    public StudentAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.textViewStudentName.setText(student.getName());

        // Используем уже рассчитанный средний балл
        holder.textViewGrade.setText(String.format("%.2f", student.getAverageGrade()));
    }

    private float calculateAverageGrade(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            return 0;
        }

        float total = 0;
        for (Grade grade : grades) {
            total += grade.getGrade();
        }
        return total / grades.size();
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }
    public void setStudentList(List<Student> newStudentList) {
        this.studentList = newStudentList;
    }
    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStudentName;
        TextView textViewGrade;
        Spinner spinnerSubjects;  // Правильное расположение

        public StudentViewHolder(View itemView) {
            super(itemView);
            textViewStudentName = itemView.findViewById(R.id.textViewStudentName);
            textViewGrade = itemView.findViewById(R.id.textViewGrade);
            spinnerSubjects = itemView.findViewById(R.id.spinnerSubjects); // Правильное объявление
        }
    }
}
