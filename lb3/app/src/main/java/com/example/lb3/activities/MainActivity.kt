package com.example.lb3.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.lb3.R
import com.example.lb3.data.PreferencesManager
import com.example.lb3.fragments.CatalogFragment
import com.example.lb3.fragments.MyLearningFragment
import com.example.lb3.fragments.ProfileFragment
import com.example.lb3.fragments.TeachersFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.lb3.network.NetworkClient

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NetworkClient.init(applicationContext)

        if (!PreferencesManager.getInstance(this).isLoggedIn()) {
            startActivity(LoginActivity.intent(this))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, 0)
            insets
        }

        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_catalog -> CatalogFragment()
                R.id.nav_my_learning -> MyLearningFragment()
                R.id.nav_teachers -> TeachersFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> CatalogFragment()
            }
            replaceFragment(fragment)
            true
        }

        if (savedInstanceState == null) {
            replaceFragment(CatalogFragment())
        }
    }

    fun navigateToCatalog() {
        bottomNav.selectedItemId = R.id.nav_catalog
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }
}
