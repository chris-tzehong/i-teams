package com.example.finalyearproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val mSettingsAboutButton: Button = findViewById(R.id.settings_about_button)
        val mSettingsSignOutButton: Button = findViewById(R.id.settings_sign_out_button)

        mSettingsAboutButton.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        mSettingsSignOutButton.setOnClickListener {
            Toast.makeText(this, R.string.sign_out_description, Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
