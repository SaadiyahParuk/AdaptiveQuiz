package com.example.adaptivegamifiedlearningsystem;

import android.provider.BaseColumns;

public final class QuizContract {

    public QuizContract() {}

    public static class QuestionsTable implements BaseColumns {
        public static final String TABLE_NAME = "quiz_questions";
        public static final String COLUMN_QUIZ_SET_ID = "quiz_set_id";
        public static final String COLUMN_QUESTION = "questions";
        public static final String COLUMN_OPTION1 = "option1";
        public static final String COLUMN_OPTION2 = "option2";
        public static final String COLUMN_OPTION3 = "option3";
        public static final String COLUMN_OPTION4 = "option4";
        public static final String COLUMN_ANSWER_NR = "answer_nr";
        public static final String COLUMN_DIFFICULTY = "difficulty";
    }

    public static class QuizSetsTable implements BaseColumns {
        public static final String TABLE_NAME = "quiz_sets";
        public static final String COLUMN_OWNER_USER_ID = "owner_user_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_UPDATED_AT = "updated_at";
    }
}
