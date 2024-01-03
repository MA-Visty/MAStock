package be.heh.projet_mastock.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import be.heh.projet_mastock.DB.MyDB
import be.heh.projet_mastock.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var db: MyDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for the activity
        setContentView(R.layout.activity_main)

        // Initialize the database instance
        db = MyDB.getDB(this)

        // Check if admin registration exists in SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("accountPref", Context.MODE_PRIVATE)
        if (!sharedPreferences.contains("adminRegistry")) {
            // If not, navigate to AdminRegisterActivity and finish current activity
            startActivity(Intent(this, AdminRegisterActivity::class.java))
            finish()
        }

        // Set up listeners to clear errors when text changes in email and password fields
        findViewById<TextInputEditText>(R.id.am_tiet_email).doOnTextChanged { text, start, before, count ->
            findViewById<TextInputLayout>(R.id.am_til_email).error = null
        }
        findViewById<TextInputEditText>(R.id.am_tiet_password).doOnTextChanged { text, start, before, count ->
            findViewById<TextInputLayout>(R.id.am_til_password).error = null
        }

        // Set up click listener for the login button
        findViewById<Button>(R.id.am_bp_login)
            .setOnClickListener {
                // Call the check function when the login button is clicked
                check()
            }

        // Set up click listener for the register button
        findViewById<Button>(R.id.am_bp_register)
            .setOnClickListener {
                // Navigate to RegisterActivity when the register button is clicked
                startActivity(Intent(this, RegisterActivity::class.java))
            }
    }

    // Function to check login credentials
    private fun check() {
        // Get email and password from input fields
        var email = findViewById<TextInputEditText>(R.id.am_tiet_email).text?.toString()
        var pwd = findViewById<TextInputEditText>(R.id.am_tiet_password).text?.toString()

        // Use coroutines for asynchronous database operations
        GlobalScope.launch {
            // Fetch user from the database based on the provided email
            val user = email?.let { db.userDao().getUser(it) }

            // Update UI on the main thread
            runOnUiThread {
                // Check if the user exists and the password matches
                if (user != null && user.pwd == pwd) {
                    // Save user information in SharedPreferences
                    val sharedPreferences = getSharedPreferences("accountPref", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putInt("id", user.id)
                    editor.putString("email", user.email)
                    editor.putBoolean("isEnable", user.isEnable)
                    editor.putBoolean("isAdmin", user.isAdmin)
                    editor.apply()

                    // Navigate to ProductsActivity on successful login and finish current activity
                    startActivity(Intent(this@MainActivity, ProductsActivity::class.java))
                    finish()
                } else {
                    // Display error messages for invalid email or password
                    findViewById<TextInputLayout>(R.id.am_til_email).error = "Invalid email!"
                    findViewById<TextInputLayout>(R.id.am_til_password).error = "Invalid password!"
                }
            }
        }
    }
}
