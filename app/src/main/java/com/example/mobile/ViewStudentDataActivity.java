package com.example.mobile;
import com.example.mobile.model.Student;
import com.example.mobile.adapter.StudentAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobile.database.DatabaseHelper;
import java.util.List;
import java.util.ArrayList;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import com.example.mobile.model.Grade;
import java.util.Collections;



public class ViewStudentDataActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private DatabaseHelper dbHelper;
    private String currentFilterSubject = null;
    private boolean showOverallAverage = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_student_data);

        Spinner spinnerFilterSubjects = findViewById(R.id.spinnerFilterSubjects);
        Button btnApplyFilter = findViewById(R.id.btnApplyFilter);
        Button buttonBack = findViewById(R.id.buttonBack);
        Button buttonResetFilters = findViewById(R.id.buttonResetFilters);
        Button buttonSortByAverageGrade = findViewById(R.id.buttonSortByAverageGrade);
        buttonSortByAverageGrade.setOnClickListener(v -> onSortByAverageGradeClicked());

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterSubjects.setAdapter(spinnerAdapter);

        btnApplyFilter.setOnClickListener(v -> {
            String selectedSubject = spinnerFilterSubjects.getSelectedItem().toString();
            applyFilter(selectedSubject);
        });
        Button buttonShareGrade = findViewById(R.id.buttonSortByOverallAverage);
        buttonShareGrade.setOnClickListener(v -> onShareGradeClicked());

        buttonBack.setOnClickListener(v -> finish());
        buttonResetFilters.setOnClickListener(v -> resetFilters());

        recyclerView = findViewById(R.id.recyclerViewStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        studentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(adapter);

        backgroundThread = new HandlerThread("DataUpdaterThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        backgroundHandler.post(updateDataRunnable);
    }

    private final Runnable updateDataRunnable = new Runnable() {
        @Override
        public void run() {
            List<Student> updatedStudents = dbHelper.getAllStudents();

            if (currentFilterSubject != null) {
                updatedStudents = filterStudentsBySubject(updatedStudents, currentFilterSubject);
            }

            if (isSortedByAverageGrade) {
                updatedStudents = sortStudentsByAverageGrade(updatedStudents);
            }

            if (showOverallAverage) {
                // Обновление для показа общего среднего балла
                // (если это необходимо в вашей логике приложения)
            }

            final List<Student> finalStudents = updatedStudents; // Финальная переменная для использования в лямбда-выражении

            runOnUiThread(() -> {
                adapter.setStudentList(finalStudents);
                adapter.notifyDataSetChanged();
            });

            backgroundHandler.postDelayed(this, 5000);
        }
    };
    private boolean isSortedByAverageGrade = false;

    private void onSortByAverageGradeClicked() {
        isSortedByAverageGrade = true;
        showOverallAverage = false;

        List<Student> studentsToSort;

        if (currentFilterSubject != null) {
            // Применяем фильтр по предмету
            studentsToSort = filterStudentsBySubject(dbHelper.getAllStudents(), currentFilterSubject);
        } else {
            // Если фильтр по предмету не применён, берём всех студентов
            studentsToSort = dbHelper.getAllStudents();
        }

        // Сортируем отфильтрованный список
        List<Student> sortedStudents = sortStudentsByAverageGrade(studentsToSort);

        // Обновляем список в адаптере
        runOnUiThread(() -> {
            adapter.setStudentList(sortedStudents);
            adapter.notifyDataSetChanged();
        });
    }
    private List<Student> sortStudentsByAverageGrade(List<Student> students) {
        List<Student> sortedStudents = new ArrayList<>(students);
        Collections.sort(sortedStudents, (student1, student2) ->
                Float.compare(student2.getAverageGrade(), student1.getAverageGrade()));
        return sortedStudents;
    }
    private void sortStudentsByAverageGrade() {
        List<Student> sortedStudents = new ArrayList<>(dbHelper.getAllStudents());
        Collections.sort(sortedStudents, (student1, student2) ->
                Float.compare(student2.getAverageGrade(), student1.getAverageGrade()));
        adapter.setStudentList(sortedStudents);
        adapter.notifyDataSetChanged();
    }
    private void applyFilter(String subject) {
        showOverallAverage = false;
        isSortedByAverageGrade = false;
        currentFilterSubject = subject;
        updateData();
    }
    private List<Student> filterStudentsBySubject(List<Student> students, String subject) {
        List<Student> filteredStudents = new ArrayList<>();
        for (Student student : students) {
            float subjectAverage = student.calculateAverageGradeForSubject(subject);
            if (subjectAverage > 0) {
                // Создаем копию студента с модифицированным списком оценок
                Student filteredStudent = new Student(student.getName(), student.getGrades(), subjectAverage);
                filteredStudents.add(filteredStudent);
            }
        }
        return filteredStudents; // Возврат отфильтрованного списка
    }

    private void resetFilters() {
        showOverallAverage = false;
        isSortedByAverageGrade = false;
        currentFilterSubject = null;
        updateData();
    }
    private void onShareGradeClicked() {
        showOverallAverage = true;
        isSortedByAverageGrade = false;
        currentFilterSubject = null;
        updateData();
    }

    private void updateData() {
        List<Student> students;
        if (showOverallAverage) {
            students = dbHelper.getAllStudents(); // Получение всех студентов для общего среднего балла
        } else if (currentFilterSubject != null) {
            students = filterStudentsBySubject(dbHelper.getAllStudents(), currentFilterSubject);
        } else {
            students = dbHelper.getAllStudents();
        }
        adapter.setStudentList(students);
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundThread.quitSafely();
    }
}




