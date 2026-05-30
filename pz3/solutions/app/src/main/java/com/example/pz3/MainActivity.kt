package com.example.pz3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTask1 = findViewById<Button>(R.id.btnTask1)
        val btnTask2 = findViewById<Button>(R.id.btnTask2)
        val btnTask3 = findViewById<Button>(R.id.btnTask3)
        val btnTask4 = findViewById<Button>(R.id.btnTask4)

        btnTask1.setOnClickListener {
            startActivity(Intent(this, CalcActivity::class.java))
        }

        btnTask2.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        btnTask3.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        btnTask4.setOnClickListener {
            startActivity(Intent(this, ConverterActivity::class.java))
        }
    }
}