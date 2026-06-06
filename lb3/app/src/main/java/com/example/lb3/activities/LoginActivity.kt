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

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkClient.init(applicationContext)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerLink = findViewById<TextView>(R.id.register_link)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val pass = passwordInput.text.toString()

            // Try server login first
            NetworkClient.login(email, pass, onSuccess = { token, user: User ->
                PreferencesManager.getInstance(this).setAuthToken(token)
                PreferencesManager.getInstance(this).setAuthUser(user)
                startActivity(MainActivity.intent(this))
                finish()
            }, onError = { err ->
                // fallback to local
                val user = PreferencesManager.getInstance(this).login(email, pass)
                if (user != null) {
                    PreferencesManager.getInstance(this).setCurrentUser(user)
                    startActivity(MainActivity.intent(this))
                    finish()
                } else {
                    Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show()
                }
            })
        }

        registerLink.setOnClickListener {
            startActivity(RegisterActivity.intent(this))
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, LoginActivity::class.java)
    }
}
