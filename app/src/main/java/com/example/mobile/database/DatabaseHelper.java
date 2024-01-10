package com.example.mobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mobile.model.Student;
import java.util.ArrayList;
import java.util.List;
import com.example.mobile.model.Grade;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "studentDatabase";
    private static final int DATABASE_VERSION = 2; // Обновляем версию базы данных

    private static final String TABLE_STUDENTS = "students";
    private static final String TABLE_GRADES = "grades";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SUBJECT = "subject";
    private static final String COLUMN_GRADE = "grade";

    private static final String TABLE_CREATE_STUDENTS =
            "CREATE TABLE " + TABLE_STUDENTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT UNIQUE);"; // Убрали subject и grade

    private static final String TABLE_CREATE_GRADES =
            "CREATE TABLE " + TABLE_GRADES + " (" +
                    COLUMN_ID + " INTEGER, " +
                    COLUMN_SUBJECT + " TEXT, " +
                    COLUMN_GRADE + " REAL, " +
                    "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_STUDENTS + "(" + COLUMN_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_STUDENTS);
        db.execSQL(TABLE_CREATE_GRADES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(TABLE_CREATE_GRADES); // Создаем новую таблицу, если обновляемся
        }
    }

    public void addStudent(Student student, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, student.getName());
        db.insert(TABLE_STUDENTS, null, values);

    }

    public void addGrade(String studentName, String subject, float grade, SQLiteDatabase db) {
        long studentId = getStudentIdByName(db, studentName);
        if (studentId != -1) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, studentId);
            values.put(COLUMN_SUBJECT, subject);
            values.put(COLUMN_GRADE, grade);
            db.insert(TABLE_GRADES, null, values);
        }
    }

    public long getStudentIdByName(SQLiteDatabase db, String name) {
        Cursor cursor = db.query(TABLE_STUDENTS, new String[]{COLUMN_ID}, COLUMN_NAME + "=?", new String[]{name}, null, null, null);
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
            return id;
        }
        cursor.close();
        return -1; // Студент не найден
    }


    public List<Student> getAllStudents() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Student> students = new ArrayList<>();
        Cursor cursor = db.query(TABLE_STUDENTS, new String[]{COLUMN_ID, COLUMN_NAME}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                List<Grade> grades = getGradesForStudent(id);
                float averageGrade = getAverageGradeForStudent(id);
                students.add(new Student(name, grades, averageGrade));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return students;
    }



    private List<Grade> getGradesForStudent(long studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Grade> grades = new ArrayList<>();
        Cursor gradeCursor = db.query(TABLE_GRADES, new String[]{COLUMN_SUBJECT, COLUMN_GRADE}, COLUMN_ID + "=?", new String[]{String.valueOf(studentId)}, null, null, null);
        if (gradeCursor.moveToFirst()) {
            do {
                String subject = gradeCursor.getString(gradeCursor.getColumnIndex(COLUMN_SUBJECT));
                float grade = gradeCursor.getFloat(gradeCursor.getColumnIndex(COLUMN_GRADE));
                grades.add(new Grade(subject, grade));
            } while (gradeCursor.moveToNext());
        }
        gradeCursor.close();
        db.close();
        return grades;
    }

    public float getAverageGradeForStudent(long studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        float averageGrade = 0f;
        Cursor cursor = db.rawQuery("SELECT AVG(" + COLUMN_GRADE + ") FROM " + TABLE_GRADES +
                " WHERE " + COLUMN_ID + "=?", new String[]{String.valueOf(studentId)});

        if (cursor.moveToFirst()) {
            averageGrade = cursor.getFloat(0);
        }
        cursor.close();
        db.close();
        return averageGrade;
    }
    public float getAverageGradeForSubject(long studentId, String subject) {
        SQLiteDatabase db = this.getReadableDatabase();
        float averageGrade = 0f;
        Cursor cursor = db.rawQuery("SELECT AVG(" + COLUMN_GRADE + ") FROM " + TABLE_GRADES +
                        " WHERE " + COLUMN_ID + "=? AND " + COLUMN_SUBJECT + "=?",
                new String[]{String.valueOf(studentId), subject});

        if (cursor.moveToFirst()) {
            averageGrade = cursor.getFloat(0);
        }
        cursor.close();
        db.close();
        return averageGrade;
    }
    public List<Student> searchStudentsByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Student> students = new ArrayList<>();
        Cursor cursor = db.query(TABLE_STUDENTS, new String[]{COLUMN_ID, COLUMN_NAME},
                COLUMN_NAME + " LIKE ?", new String[]{"%" + name + "%"},
                null, null, null);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            String studentName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            List<Grade> grades = getGradesForStudent(id);
            float averageGrade = getAverageGradeForStudent(id); // Рассчитываем средний балл
            students.add(new Student(studentName, grades, averageGrade));
        }
        cursor.close();
        db.close();
        return students;
    }

    public List<Student> sortStudentsByAverageGrade() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Student> students = new ArrayList<>();

        String query = "SELECT " + TABLE_STUDENTS + "." + COLUMN_ID + ", " + TABLE_STUDENTS + "." + COLUMN_NAME + ", AVG(" + TABLE_GRADES + "." + COLUMN_GRADE + ") as averageGrade " +
                "FROM " + TABLE_STUDENTS +
                " JOIN " + TABLE_GRADES + " ON " + TABLE_STUDENTS + "." + COLUMN_ID + " = " + TABLE_GRADES + "." + COLUMN_ID +
                " GROUP BY " + TABLE_STUDENTS + "." + COLUMN_ID +
                " ORDER BY averageGrade DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                float averageGrade = cursor.getFloat(cursor.getColumnIndex("averageGrade"));
                List<Grade> grades = getGradesForStudent(id);
                students.add(new Student(name, grades, averageGrade));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return students;
    }

    public List<Student> sortStudentsBySubjectGrade(String subject) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Student> students = new ArrayList<>();

        String query = "SELECT " + TABLE_STUDENTS + "." + COLUMN_ID + ", " + TABLE_STUDENTS + "." + COLUMN_NAME + ", AVG(" + TABLE_GRADES + "." + COLUMN_GRADE + ") as averageGrade " +
                "FROM " + TABLE_STUDENTS +
                " JOIN " + TABLE_GRADES + " ON " + TABLE_STUDENTS + "." + COLUMN_ID + " = " + TABLE_GRADES + "." + COLUMN_ID +
                " WHERE " + TABLE_GRADES + "." + COLUMN_SUBJECT + " = ?" +
                " GROUP BY " + TABLE_STUDENTS + "." + COLUMN_ID +
                " ORDER BY averageGrade DESC";

        Cursor cursor = db.rawQuery(query, new String[]{subject});

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                float averageGrade = cursor.getFloat(cursor.getColumnIndex("averageGrade"));
                List<Grade> grades = getGradesForStudent(id);
                students.add(new Student(name, grades, averageGrade));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return students;

    }


}
