package com.example.pz3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConverterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter)

        val romanInput = findViewById<EditText>(R.id.romanInput)
        val convertButton = findViewById<Button>(R.id.convertButton)
        val arabicResultText = findViewById<TextView>(R.id.arabicResultText)
        val btnBack = findViewById<Button>(R.id.btnBack)

        convertButton.setOnClickListener {
            val roman = romanInput.text.toString().trim().uppercase()
            if (roman.isEmpty()) {
                Toast.makeText(this, "Please enter a Roman numeral", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val result = romanToArabic(roman)
                arabicResultText.text = "Arabic Number: $result"
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid Roman numeral", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun romanToArabic(s: String): Int {
        val romanMap = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
        var total = 0
        var prevValue = 0
        for (i in s.length - 1 downTo 0) {
            val currentValue = romanMap[s[i]] ?: throw IllegalArgumentException("Invalid character")
            if (currentValue < prevValue) {
                total -= currentValue
            } else {
                total += currentValue
            }
            prevValue = currentValue
        }
        return total
    }
}
