package be.heh.projet_mastock.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import be.heh.projet_mastock.DB.MyDB
import be.heh.projet_mastock.DB.User
import be.heh.projet_mastock.DB.UserRecord
import be.heh.projet_mastock.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class AdminRegisterActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: MyDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for the activity
        setContentView(R.layout.activity_register)

        // Initialize the database instance
        db = MyDB.getDB(this)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("accountPref", Context.MODE_PRIVATE)

        // Check if admin registration exists in SharedPreferences
        checkAdminPref()

        // Set title for the activity
        findViewById<MaterialTextView>(R.id.ar_mtv_title).setText("Create account administrator")

        // Set up listeners to clear errors when text changes in email and password fields
        findViewById<TextInputEditText>(R.id.ar_tiet_email).doOnTextChanged { text, start, before, count ->
            findViewById<TextInputLayout>(R.id.ar_til_email).error = null
        }
        findViewById<TextInputEditText>(R.id.ar_tiet_password).doOnTextChanged { text, start, before, count ->
            findViewById<TextInputLayout>(R.id.ar_til_password).error = null
        }
        findViewById<TextInputEditText>(R.id.ar_tiet_password_confirm).doOnTextChanged { text, start, before, count ->
            findViewById<TextInputLayout>(R.id.ar_til_password_confirm).error = null
        }

        // Set up click listener for the confirm button
        findViewById<Button>(R.id.ar_bp_confirm)
            .setOnClickListener {
                // Check data validity and navigate to the main activity if data is valid
                if (checkData()) {
                    toMain()
                }
            }

        // Set up click listener for the cancel button
        findViewById<Button>(R.id.ar_bp_cancel)
            .setOnClickListener {
                // Finish the activity
                finish()
            }

        // Set the text for the cancel button
        findViewById<Button>(R.id.ar_bp_cancel).setText("Exit")
    }

    // Function to check the validity of entered data
    fun checkData(): Boolean {
        var email = findViewById<TextInputEditText>(R.id.ar_tiet_email).text?.toString()
        var pwd1 = findViewById<TextInputEditText>(R.id.ar_tiet_password).text?.toString()
        var pwd2 = findViewById<TextInputEditText>(R.id.ar_tiet_password_confirm).text?.toString()

        // Define a regular expression pattern for a valid email
        val emailPattern = Pattern.compile("^[a-zA-Z0-9._-]+@(heh\\.be|std\\.heh\\.be)$")

        // Validate email format
        if (email.isNullOrBlank() || !emailPattern.matcher(email).matches()) {
            findViewById<TextInputLayout>(R.id.ar_til_email).error = "Invalid email!"
            return false
        }

        // Validate password format and matching
        if (pwd1.isNullOrBlank() || pwd2.isNullOrBlank() || pwd1 != pwd2 || pwd1.length < 4 || pwd2.length < 4) {
            findViewById<TextInputLayout>(R.id.ar_til_password).error = "Invalid password!"
            findViewById<TextInputLayout>(R.id.ar_til_password_confirm).error = "Invalid password!"
            return false
        }

        try {
            // Save admin data and update SharedPreferences
            saveAdmin(email, pwd1)
            saveAdminPref()
            return true
        } catch (error: Error) {
            Log.i("Error creating admin!", error.toString())
        }
        return false
    }

    // Function to navigate to the main activity
    fun toMain() {
        // Start the main activity and display a toast message
        startActivity(Intent(this, MainActivity::class.java))
        Toast.makeText(getApplicationContext(), "Admin already exists", Toast.LENGTH_SHORT).show()
        finish()
    }

    // Function to check if admin registration exists in SharedPreferences
    fun checkAdminPref() {
        if (sharedPreferences.contains("adminRegistry")) {
            // If admin registration exists, navigate to the main activity
            toMain()
        }
    }

    // Function to save admin registration status in SharedPreferences
    fun saveAdminPref() {
        var editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("adminRegistry", true)
        editor.commit()
    }

    // Function to save admin data in the database
    fun saveAdmin(email: String, pwd: String) {
        val user = User(0, email, pwd, true, true)
        // Use coroutines for asynchronous database operations
        GlobalScope.launch {
            db.userDao().insertUser(UserRecord(0, user.email, user.pwd, user.isEnable, user.isAdmin))
            runOnUiThread {
                // Display a toast message indicating successful admin creation
                Toast.makeText(applicationContext, "Admin ${user.email} is created.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
