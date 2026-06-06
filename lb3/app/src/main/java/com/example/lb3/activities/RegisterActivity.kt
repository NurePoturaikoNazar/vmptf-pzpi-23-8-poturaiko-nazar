package com.example.lb3.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lb3.R
import com.example.lb3.data.PreferencesManager
import com.example.lb3.network.NetworkClient
import com.example.lb3.models.User

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkClient.init(applicationContext)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val nameInput = findViewById<EditText>(R.id.name_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginLink = findViewById<TextView>(R.id.login_link)

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val pass = passwordInput.text.toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            NetworkClient.register(name, email, pass, onSuccess = { token, user: User ->
                PreferencesManager.getInstance(this).setAuthToken(token)
                PreferencesManager.getInstance(this).setAuthUser(user)
                startActivity(MainActivity.intent(this))
                finishAffinity()
            }, onError = { err ->
                // fallback to local register
                val user = PreferencesManager.getInstance(this).registerUser(name, email, pass)
                if (user != null) {
                    PreferencesManager.getInstance(this).setCurrentUser(user)
                    startActivity(MainActivity.intent(this))
                    finishAffinity()
                } else {
                    Toast.makeText(this, R.string.error_email_taken, Toast.LENGTH_SHORT).show()
                }
            })
        }

        loginLink.setOnClickListener { finish() }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, RegisterActivity::class.java)
    }
}
