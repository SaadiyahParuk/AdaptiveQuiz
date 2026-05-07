package com.example.adaptivegamifiedlearningsystem;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FinalScoreDialog {
    private Context mContext;
    private Dialog finalScoreDialog;

    private TextView textViewFinalScore;
    private TextView textViewCorrect;
    private TextView textViewIncorrect;

    public FinalScoreDialog(Context context) {
        mContext = context;
    }

    public void showFinalScoreDialog(int quizLength, int correctAnswers, int incorrectAnswers) {
        finalScoreDialog = new Dialog(mContext);
        finalScoreDialog.setContentView(R.layout.final_score_dialogue);

        final Button btnFinalScore = (Button) finalScoreDialog.findViewById(R.id.btnFinalScore);

        finalScoreCalc(quizLength, correctAnswers, incorrectAnswers);
        btnFinalScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalScoreDialog.dismiss();

                Intent intent = new Intent(mContext, HomeActivity.class);
                mContext.startActivity(intent);
            }
        });

        finalScoreDialog.show();
        finalScoreDialog.setCancelable(false);
        finalScoreDialog.setCanceledOnTouchOutside(false);
    }

    private void finalScoreCalc(int quizLength, int correctAnswers, int incorrectAnswers) {
        int tempScore;
        textViewFinalScore = (TextView) finalScoreDialog.findViewById(R.id.txtFinalScore);
        textViewCorrect = (TextView) finalScoreDialog.findViewById(R.id.txtCorrect);
        textViewIncorrect = (TextView) finalScoreDialog.findViewById(R.id.txtIncorrect);

        if (incorrectAnswers == quizLength) {
            tempScore = 0;
            textViewFinalScore.setText("Final Score: " + tempScore);
        } else {
            tempScore = (correctAnswers * 20) - (incorrectAnswers * 5);
            textViewFinalScore.setText("Final Score: " + tempScore);
        }

        textViewCorrect.setText("Correct: " + correctAnswers);
        textViewIncorrect.setText("Wrong: " + incorrectAnswers);
    }

}
