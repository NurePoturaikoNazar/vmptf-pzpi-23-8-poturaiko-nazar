package com.example.pz3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private val secretWord = "KOTLIN"
    private var displayedWord = CharArray(secretWord.length) { '*' }
    private var attemptsLeft = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val wordDisplay = findViewById<TextView>(R.id.wordDisplay)
        val attemptsText = findViewById<TextView>(R.id.attemptsText)
        val letterInput = findViewById<EditText>(R.id.letterInput)
        val guessButton = findViewById<Button>(R.id.guessButton)
        val btnBack = findViewById<Button>(R.id.btnBack)

        wordDisplay.text = String(displayedWord)

        guessButton.setOnClickListener {
            val input = letterInput.text.toString().trim().uppercase()
            letterInput.text.clear()

            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter a letter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val guessedLetter = input[0]
            var hit = false

            for (i in secretWord.indices) {
                if (secretWord[i] == guessedLetter) {
                    displayedWord[i] = guessedLetter
                    hit = true
                }
            }

            if (hit) {
                wordDisplay.text = String(displayedWord)
                if (!String(displayedWord).contains('*')) {
                    Toast.makeText(this, "You won! Word is $secretWord", Toast.LENGTH_LONG).show()
                    guessButton.isEnabled = false
                }
            } else {
                attemptsLeft--
                attemptsText.text = "Attempts left: $attemptsLeft"
                if (attemptsLeft <= 0) {
                    Toast.makeText(this, "Game Over! Word was $secretWord", Toast.LENGTH_LONG).show()
                    guessButton.isEnabled = false
                }
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
