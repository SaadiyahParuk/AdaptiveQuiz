package com.example.adaptivegamifiedlearningsystem;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import com.example.adaptivegamifiedlearningsystem.QuizContract.*;

import java.util.ArrayList;
import java.util.List;

public class QuizDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "Quiz.db";
    public static final int DB_VERSION = 3;

    private SQLiteDatabase db;
    public QuizDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        db.execSQL("CREATE TABLE " + QuizSetsTable.TABLE_NAME + " (" +
                QuizSetsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuizSetsTable.COLUMN_OWNER_USER_ID + " TEXT, " +
                QuizSetsTable.COLUMN_TITLE + " TEXT, " +
                QuizSetsTable.COLUMN_DESCRIPTION + " TEXT, " +
                QuizSetsTable.COLUMN_UPDATED_AT + " INTEGER)");

        db.execSQL("CREATE TABLE " + QuestionsTable.TABLE_NAME + " (" +
                QuestionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionsTable.COLUMN_QUIZ_SET_ID + " INTEGER, " +
                QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                QuestionsTable.COLUMN_OPTION1 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION2 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION3 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION4 + " TEXT, " +
                QuestionsTable.COLUMN_ANSWER_NR + " INTEGER, " +
                QuestionsTable.COLUMN_DIFFICULTY + " INTEGER DEFAULT 2, " +
                "FOREIGN KEY(" + QuestionsTable.COLUMN_QUIZ_SET_ID + ") REFERENCES " +
                QuizSetsTable.TABLE_NAME + "(" + QuizSetsTable._ID + ") ON DELETE CASCADE)");

        seedExampleSet();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);   // for the CASCADE above
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME);
        onCreate(db);
    }

    private void seedExampleSet() {
        ContentValues sv = new ContentValues();
        sv.put(QuizSetsTable.COLUMN_OWNER_USER_ID, "example");
        sv.put(QuizSetsTable.COLUMN_TITLE, "Geography (sample)");
        sv.put(QuizSetsTable.COLUMN_DESCRIPTION, "Capitals of European countries");
        sv.put(QuizSetsTable.COLUMN_UPDATED_AT, System.currentTimeMillis());
        long setId = db.insert(QuizSetsTable.TABLE_NAME, null, sv);

        addQuestion(setId, new Questions("What is the capital of France?", "London","Berlin","Paris","Madrid", 3), 1);
        addQuestion(setId, new Questions("What is the capital of England?", "London","Berlin","Paris","Madrid", 1), 1);
        addQuestion(setId, new Questions("What is the capital of Italy?", "Rome","Lisbon","Vienna","Bern", 1), 1);
        addQuestion(setId, new Questions("What is the capital of Spain?", "London","Berlin","Paris","Madrid", 4), 1);
        addQuestion(setId, new Questions("What is the capital of Germany?", "London","Berlin","Paris","Madrid", 2), 1);
        addQuestion(setId, new Questions("What is the capital of Portugal?", "Rome","Lisbon","Vienna","Bern", 2), 2);
        addQuestion(setId, new Questions("What is the capital of Switzerland?", "Rome","Lisbon","Vienna","Bern", 4), 2);
        addQuestion(setId, new Questions("What is the capital of Austria?", "Rome","Lisbon","Vienna","Bern", 3), 2);
        addQuestion(setId, new Questions("What is the capital of Belgium?", "Brussels","Berlin","Amsterdam","Madrid", 1), 3);
        addQuestion(setId, new Questions("What is the capital of Netherlands?", "Brussels","Berlin","Amsterdam","Madrid", 3), 3);
    }

    private void addQuestion(long quizSetId, Questions q, int difficulty) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsTable.COLUMN_QUIZ_SET_ID, quizSetId);
        cv.put(QuestionsTable.COLUMN_QUESTION, q.getQuestion());
        cv.put(QuestionsTable.COLUMN_OPTION1, q.getOption1());
        cv.put(QuestionsTable.COLUMN_OPTION2, q.getOption2());
        cv.put(QuestionsTable.COLUMN_OPTION3, q.getOption3());
        cv.put(QuestionsTable.COLUMN_OPTION4, q.getOption4());
        cv.put(QuestionsTable.COLUMN_ANSWER_NR, q.getAnswerNr());
        cv.put(QuestionsTable.COLUMN_DIFFICULTY, difficulty);
        db.insert(QuestionsTable.TABLE_NAME, null, cv);
    }

    @SuppressLint("Range")
    public ArrayList<Questions> getAllQuestions(){
        ArrayList<Questions> questionsList = new ArrayList<>();
        db = getReadableDatabase();
        String Projection[] = {
                QuestionsTable._ID,
                QuestionsTable.COLUMN_QUESTION,
                QuestionsTable.COLUMN_OPTION1,
                QuestionsTable.COLUMN_OPTION2,
                QuestionsTable.COLUMN_OPTION3,
                QuestionsTable.COLUMN_OPTION4,
                QuestionsTable.COLUMN_ANSWER_NR
        };
        Cursor c = db.query(
                QuestionsTable.TABLE_NAME,
                Projection,
                null,
                null,
                null,
                null,
                null
        );

        if(c.moveToFirst()){
            do{
                Questions questions = new Questions();
                questions.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                questions.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                questions.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                questions.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                questions.setOption4(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION4)));
                questions.setAnswerNr(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                questionsList.add(questions);

            } while (c.moveToNext());
        }
        c.close();
        return questionsList;
    }

    // CRUD
    public long insertQuizSet(QuizSet s) {
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QuizSetsTable.COLUMN_OWNER_USER_ID, s.getOwnerUserId());
        cv.put(QuizSetsTable.COLUMN_TITLE, s.getTitle());
        cv.put(QuizSetsTable.COLUMN_DESCRIPTION, s.getDescription());
        cv.put(QuizSetsTable.COLUMN_UPDATED_AT, System.currentTimeMillis());
        return db.insert(QuizSetsTable.TABLE_NAME, null, cv);
    }

    public int updateQuizSet(QuizSet s) {
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QuizSetsTable.COLUMN_TITLE, s.getTitle());
        cv.put(QuizSetsTable.COLUMN_DESCRIPTION, s.getDescription());
        cv.put(QuizSetsTable.COLUMN_UPDATED_AT, System.currentTimeMillis());
        return db.update(QuizSetsTable.TABLE_NAME, cv,
                QuizSetsTable._ID + " = ?", new String[]{ String.valueOf(s.getId()) });
    }

    public int deleteQuizSet(long id) {
        db = getWritableDatabase();
        return db.delete(QuizSetsTable.TABLE_NAME,
                QuizSetsTable._ID + " = ?", new String[]{ String.valueOf(id) });
    }

    @SuppressLint("Range")
    public ArrayList<QuizSet> getQuizSetsForUser(String ownerUserId) {
        ArrayList<QuizSet> list = new ArrayList<>();
        db = getReadableDatabase();
        // Show user's own sets PLUS the "example" sample
        Cursor c = db.query(QuizSetsTable.TABLE_NAME, null,
                QuizSetsTable.COLUMN_OWNER_USER_ID + " IN (?, 'example')",
                new String[]{ ownerUserId }, null, null,
                QuizSetsTable.COLUMN_UPDATED_AT + " DESC");
        if (c.moveToFirst()) {
            do {
                list.add(new QuizSet(
                        c.getLong(c.getColumnIndex(QuizSetsTable._ID)),
                        c.getString(c.getColumnIndex(QuizSetsTable.COLUMN_OWNER_USER_ID)),
                        c.getString(c.getColumnIndex(QuizSetsTable.COLUMN_TITLE)),
                        c.getString(c.getColumnIndex(QuizSetsTable.COLUMN_DESCRIPTION)),
                        c.getLong(c.getColumnIndex(QuizSetsTable.COLUMN_UPDATED_AT))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    // CRUD Databases
    public long insertQuestion(long quizSetId, Questions q, int difficulty) {
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QuestionsTable.COLUMN_QUIZ_SET_ID, quizSetId);
        cv.put(QuestionsTable.COLUMN_QUESTION, q.getQuestion());
        cv.put(QuestionsTable.COLUMN_OPTION1, q.getOption1());
        cv.put(QuestionsTable.COLUMN_OPTION2, q.getOption2());
        cv.put(QuestionsTable.COLUMN_OPTION3, q.getOption3());
        cv.put(QuestionsTable.COLUMN_OPTION4, q.getOption4());
        cv.put(QuestionsTable.COLUMN_ANSWER_NR, q.getAnswerNr());
        cv.put(QuestionsTable.COLUMN_DIFFICULTY, difficulty);
        return db.insert(QuestionsTable.TABLE_NAME, null, cv);
    }

    public int updateQuestion(Questions q) {
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QuestionsTable.COLUMN_QUESTION, q.getQuestion());
        cv.put(QuestionsTable.COLUMN_OPTION1, q.getOption1());
        cv.put(QuestionsTable.COLUMN_OPTION2, q.getOption2());
        cv.put(QuestionsTable.COLUMN_OPTION3, q.getOption3());
        cv.put(QuestionsTable.COLUMN_OPTION4, q.getOption4());
        cv.put(QuestionsTable.COLUMN_ANSWER_NR, q.getAnswerNr());
        cv.put(QuestionsTable.COLUMN_DIFFICULTY, q.getDifficulty());
        return db.update(QuestionsTable.TABLE_NAME, cv,
                QuestionsTable._ID + " = ?", new String[]{ String.valueOf(q.getId()) });
    }

    public int deleteQuestion(long id) {
        db = getWritableDatabase();
        return db.delete(QuestionsTable.TABLE_NAME,
                QuestionsTable._ID + " = ?", new String[]{ String.valueOf(id) });
    }

    @SuppressLint("Range")
    public ArrayList<Questions> getQuestionsForSet(long quizSetId) {
        ArrayList<Questions> list = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.query(QuestionsTable.TABLE_NAME, null,
                QuestionsTable.COLUMN_QUIZ_SET_ID + " = ?",
                new String[]{ String.valueOf(quizSetId) }, null, null, null);
        if (c.moveToFirst()) {
            do {
                Questions q = new Questions();
                q.setId(c.getLong(c.getColumnIndex(QuestionsTable._ID)));
                q.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                q.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                q.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                q.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                q.setOption4(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION4)));
                q.setAnswerNr(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                q.setDifficulty(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_DIFFICULTY)));
                list.add(q);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }
}
