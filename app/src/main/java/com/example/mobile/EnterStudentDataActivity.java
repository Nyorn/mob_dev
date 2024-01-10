package com.example.mobile;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.example.mobile.database.DatabaseHelper;
import com.example.mobile.model.Student;
import java.util.ArrayList;
import android.widget.Toast;
import com.example.mobile.model.Grade;


public class EnterStudentDataActivity extends AppCompatActivity {

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private DatabaseHelper dbHelper;
    private Button btnSave;
    private EditText editTextName;
    private EditText editTextGrade;
    private Spinner spinnerSubjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_student_data);

        spinnerSubjects = findViewById(R.id.spinnerSubjects);
        btnSave = findViewById(R.id.btnSave);
        editTextName = findViewById(R.id.editTextName);
        editTextGrade = findViewById(R.id.editTextGrade);
        Button buttonBack = findViewById(R.id.buttonBack);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubjects.setAdapter(adapter);

        dbHelper = new DatabaseHelper(this);

        backgroundThread = new HandlerThread("DataProcessingThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        btnSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String subject = spinnerSubjects.getSelectedItem().toString();
            float gradeValue;

            if (name.length() > 24) {
                Toast.makeText(this, "Имя не должно превышать 24 символа.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                gradeValue = Float.parseFloat(editTextGrade.getText().toString());
                if (gradeValue < 0 || gradeValue > 6) {
                    Toast.makeText(this, "Оценка должна быть от 0 до 6.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Оценка должна быть числом.", Toast.LENGTH_SHORT).show();
                return;
            }

            backgroundHandler.post(() -> processStudentData(name, subject, gradeValue));
        });
        buttonBack.setOnClickListener(v -> finish()); // Обработчик кнопки "Back"
    }

    private void processStudentData(String name, String subject, float grade) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            long studentId = dbHelper.getStudentIdByName(db, name);
            Log.d("EnterStudentData", "Student ID: " + studentId);

            if (studentId == -1) {
                Student newStudent = new Student(name, new ArrayList<>(), 0f);
                dbHelper.addStudent(newStudent, db);
                studentId = dbHelper.getStudentIdByName(db, name);
                Log.d("EnterStudentData", "New student added with ID: " + studentId);
            }

            if (studentId != -1) {
                dbHelper.addGrade(name, subject, grade, db);
                Log.d("EnterStudentData", "Grade added for student ID: " + studentId);
            }

            runOnUiThread(() -> editTextGrade.setText(""));
        } catch (Exception e) {
            Log.e("EnterStudentData", "Error in processing student data", e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundThread.quitSafely();
    }
}
