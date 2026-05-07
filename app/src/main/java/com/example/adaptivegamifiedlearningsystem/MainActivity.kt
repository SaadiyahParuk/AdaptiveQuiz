package com.example.adaptivegamifiedlearningsystem

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.nio.Buffer
import java.util.Collections
import androidx.lifecycle.lifecycleScope
import com.example.adaptivegamifiedlearningsystem.network.Network
import com.example.adaptivegamifiedlearningsystem.network.PredictRequest
import kotlinx.coroutines.launch
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton1: RadioButton
    private lateinit var radioButton2: RadioButton
    private lateinit var radioButton3: RadioButton
    private lateinit var radioButton4: RadioButton
    private lateinit var buttonConfirmNext: Button

    private lateinit var textViewQuestions: TextView
    private lateinit var textViewScore: TextView
    private lateinit var textViewQuestionCount: TextView
    private lateinit var textViewCorrect: TextView
    private lateinit var textViewIncorrect: TextView
    private lateinit var textViewErrorMessage: TextView

    private lateinit var questionsList: ArrayList<Questions>
    private lateinit var currentQuestion: Questions
    private var questionCounter: Int = 0
    private var questionCountTotal: Int = 0
    private var answered: Boolean = false
    private var score: Int = 0
    private var correctScore: Int = 0
    private var incorrectScore: Int = 0
    private var quizSetId: Long = -1L
    private var previousAttemptScore: Int = 0   // becomes G2
    private var elapsedAtStart: Long = 0L
    private val recentlyShownIds = mutableSetOf<Long>()
    private var nextDifficulty: Int = 2  // 1..3, starts medium

    private val handler: Handler = Handler()
    private lateinit var buttonLabelColor: ColorStateList
    private lateinit var finalScoreDialog: FinalScoreDialog
    private var quizLength: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        quizSetId = intent.getLongExtra("quizSetId", -1L)
        if (quizSetId == -1L) { finish(); return }   // launched without context
        elapsedAtStart = System.currentTimeMillis()

        setupUI()
        fetchDB()

        buttonLabelColor = radioButton1.getTextColors()

        finalScoreDialog = FinalScoreDialog(this)
    }

    private fun setupUI() {
        textViewCorrect = findViewById(R.id.txtCorrect)
        textViewIncorrect = findViewById(R.id.txtIncorrect)
        textViewScore = findViewById(R.id.txtScore)
        textViewQuestionCount = findViewById(R.id.txtTotalQuestions2)
        textViewQuestions = findViewById(R.id.txtQuestion)
        textViewErrorMessage = findViewById(R.id.txtErrorMessage)

        buttonConfirmNext = findViewById(R.id.button)
        radioGroup = findViewById(R.id.radioGroup)
        radioButton1 = findViewById(R.id.radioButton1)
        radioButton2 = findViewById(R.id.radioButton2)
        radioButton3 = findViewById(R.id.radioButton3)
        radioButton4 = findViewById(R.id.radioButton4)
    }

    private fun fetchDB() {
        val dbHelper = QuizDbHelper(this)
        questionsList = dbHelper.getQuestionsForSet(quizSetId)
        quizLength = questionsList.size
        startQuiz()
    }

    private fun startQuiz() {
        questionCountTotal = questionsList.size
        questionsList.shuffle()

        showQuestion()

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val buttons = listOf(radioButton1, radioButton2, radioButton3, radioButton4)

            buttons.forEach {
                it.background = ContextCompat.getDrawable(applicationContext, R.drawable.buttons_background)
                it.setTextColor(buttonLabelColor)
            }

            when (checkedId) {
                R.id.radioButton1 -> radioButton1.background = ContextCompat.getDrawable(applicationContext, R.drawable.when_selected)
                R.id.radioButton2 -> radioButton2.background = ContextCompat.getDrawable(applicationContext, R.drawable.when_selected)
                R.id.radioButton3 -> radioButton3.background = ContextCompat.getDrawable(applicationContext, R.drawable.when_selected)
                R.id.radioButton4 -> radioButton4.background = ContextCompat.getDrawable(applicationContext, R.drawable.when_selected)
            }
        }

        buttonConfirmNext.setOnClickListener {
            if (!answered) {
                if (radioButton1.isChecked || radioButton2.isChecked || radioButton3.isChecked || radioButton4.isChecked) {
                    quizOperation()
                } else {
                    handler.postDelayed({
                        textViewErrorMessage.setText("Please select an answer")
                    }, 500)
                }
            } else {
                showQuestion()
            }
        }
    }

    private fun quizOperation() {

        answered = true
        val rbSelected: RadioButton = findViewById(radioGroup.getCheckedRadioButtonId())
        val answerNr: Int = radioGroup.indexOfChild(rbSelected) + 1

        checkSolution(answerNr,rbSelected)
    }

    private fun checkSolution(answerNr: Int, rbSelected: RadioButton) {
        val selectedButtonAnswer = when (currentQuestion.getAnswerNr()) {
            1 -> radioButton1
            2 -> radioButton2
            3 -> radioButton3
            4 -> radioButton4
            else -> null
        }

        selectedButtonAnswer?.let {
            it.background = ContextCompat.getDrawable(this, R.drawable.when_correct)
            it.setTextColor(Color.WHITE)
        }

        if (answerNr == currentQuestion.getAnswerNr()) {
            correctScore++
            score += 20
            textViewCorrect.text = "Correct: $correctScore"
        }

        if (answerNr != currentQuestion.getAnswerNr()) {
            changeToIncorrect(rbSelected)
            incorrectScore++
            score -= 5
            textViewIncorrect.text = "Wrong: $incorrectScore"
        }

        textViewScore.text = "Score: $score"

        if (questionCounter >= questionCountTotal) {
            buttonConfirmNext.text = "Finish Quiz"
        } else {
            buttonConfirmNext.text = "Confirm"
        }

        callApiAndAdjust()

        handler.postDelayed({
            if (questionCounter < questionCountTotal) {
                showQuestion()
            } else {
                finalScoreDialog.showFinalScoreDialog(quizLength, correctScore, incorrectScore);
            }
        }, 500)
    }


    private fun changeToIncorrect(rbSelected: RadioButton) {
        rbSelected.setBackground(ContextCompat.getDrawable(this, R.drawable.when_incorrect))
        rbSelected.setTextColor(Color.WHITE)
    }

    private fun showQuestion() {
        radioGroup.clearCheck()

        val pool = questionsList
            .filter { it.id !in recentlyShownIds && it.difficulty == nextDifficulty }
            .ifEmpty { questionsList.filter { it.id !in recentlyShownIds } }

        if (pool.isEmpty()) {
            finalScoreDialog.showFinalScoreDialog(quizLength, correctScore, incorrectScore)
            return
        }

        currentQuestion = pool.random()
        recentlyShownIds.add(currentQuestion.id)

        textViewQuestions.text = currentQuestion.question
        radioButton1.text = currentQuestion.option1
        radioButton2.text = currentQuestion.option2
        radioButton3.text = currentQuestion.option3
        radioButton4.text = currentQuestion.option4

        questionCounter++
        answered = false
        buttonConfirmNext.text = "Confirm"
        textViewQuestionCount.text = "Questions: $questionCounter/$quizLength"
    }

    private fun callApiAndAdjust() {
        val elapsed = System.currentTimeMillis() - elapsedAtStart
        val secondsPerQ = if (questionCounter > 0) (elapsed / 1000.0) / questionCounter else 0.0
        val studytime = when {
            secondsPerQ < 10 -> 1; secondsPerQ < 20 -> 2; secondsPerQ < 40 -> 3; else -> 4
        }
        val failures = if (questionCounter > 0)
            ((incorrectScore.toDouble()) * 5).toInt()
        else incorrectScore
        val absences = 0
        val g1 = if (questionCounter > 0)
            ((correctScore.toDouble()) * 20).toInt()
        else 0
        val g2 = previousAttemptScore

        val features = listOf(studytime, failures, absences, g1, g2)

        previousAttemptScore = g1

        Log.d("QuizAPI", "Sending features: $features")
        lifecycleScope.launch {
            try {
                val response = Network.api.predict(PredictRequest(features))
                Log.d("QuizAPI", "Got result=${response.result}, action=${response.adaptiveAction}")
                nextDifficulty = when {
                    response.adaptiveAction.contains("easier", true) ||
                            response.adaptiveAction.contains("remedial", true) ||
                            response.adaptiveAction.contains("support", true) -> 1
                    response.adaptiveAction.contains("harder", true) ||
                            response.adaptiveAction.contains("advance", true) ||
                            response.adaptiveAction.contains("challenge", true) -> 3
                    else -> 2
                }
                Log.d("QuizAPI", "Next difficulty set to: $nextDifficulty")
            } catch (e: Exception) {
                Log.e("QuizAPI", "API call failed", e)
            }
        }
    }
}