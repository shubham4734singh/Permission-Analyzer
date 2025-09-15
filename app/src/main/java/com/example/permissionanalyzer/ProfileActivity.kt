package com.example.permissionanalyzer // ✅ Keep your package name

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView

class ProfileActivity : AppCompatActivity() {

    private lateinit var darkModeSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        initializeViews()
        setupDarkModeSwitch()

        // 🔹 Back button (closes this activity)
        findViewById<CardView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // 🔹 GitHub link
        findViewById<TextView>(R.id.linkGithub).setOnClickListener {
            openUrl("https://github.com/shubham4734singh")
        }

        // 🔹 LinkedIn link
        findViewById<TextView>(R.id.linkLinkedin).setOnClickListener {
            openUrl("https://www.linkedin.com/in/shubham4734singh/")
        }

        // 🔹 Portfolio link (replace with your actual website)
        findViewById<TextView>(R.id.linkPortfolio).setOnClickListener {
            openUrl("https://yourwebsite.com")
        }

        // 🔹 Contact Me button
        findViewById<Button>(R.id.btnContact).setOnClickListener {
            sendEmail("shubhamsingh9974525390@email.com")
        }
    }

    private fun initializeViews() {
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
    }

    private fun setupDarkModeSwitch() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        darkModeSwitch.isChecked = isDarkMode

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            Toast.makeText(this, "Theme changed", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Opens a browser for a given URL
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No browser found to open this link", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Opens email client
    private fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}
