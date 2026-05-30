package com.example.pz3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CalcActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calc)

        val number1 = findViewById<EditText>(R.id.number1)
        val number2 = findViewById<EditText>(R.id.number2)
        val calcButton = findViewById<Button>(R.id.calcButton)
        val resultText = findViewById<TextView>(R.id.resultText)
        val btnBack = findViewById<Button>(R.id.btnBack)

        calcButton.setOnClickListener {
            val str1 = number1.text.toString()
            val str2 = number2.text.toString()

            val d1 = str1.toDoubleOrNull()
            val d2 = str2.toDoubleOrNull()

            if (d1 == null || d2 == null) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            } else {
                val sum = d1 + d2
                resultText.text = "Result: $sum"
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
