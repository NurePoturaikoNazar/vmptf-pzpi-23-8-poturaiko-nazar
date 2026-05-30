package com.example.pz3

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {

    private var selectedDate = ""
    private val eventsMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val selectDateButton = findViewById<Button>(R.id.selectDateButton)
        val selectedDateText = findViewById<TextView>(R.id.selectedDateText)
        val eventInput = findViewById<EditText>(R.id.eventInput)
        val saveEventButton = findViewById<Button>(R.id.saveEventButton)
        val savedEventsDisplay = findViewById<TextView>(R.id.savedEventsDisplay)
        val btnBack = findViewById<Button>(R.id.btnBack)

        selectDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                selectedDateText.text = "Selected Date: $selectedDate"
                
                val existingEvent = eventsMap[selectedDate]
                savedEventsDisplay.text = "Saved Events:\n${existingEvent ?: "No events for this day"}"
            }, year, month, day)

            datePickerDialog.show()
        }

        saveEventButton.setOnClickListener {
            val eventDescription = eventInput.text.toString().trim()

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (eventDescription.isEmpty()) {
                Toast.makeText(this, "Please enter event description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            eventsMap[selectedDate] = eventDescription
            eventInput.text.clear()
            savedEventsDisplay.text = "Saved Events:\n$selectedDate: $eventDescription"
            Toast.makeText(this, "Event saved successfully", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
