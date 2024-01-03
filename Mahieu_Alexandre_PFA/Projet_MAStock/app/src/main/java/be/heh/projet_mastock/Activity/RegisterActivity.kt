package be.heh.projet_mastock.Activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import be.heh.projet_mastock.DB.MyDB
import be.heh.projet_mastock.DB.UserRecord
import be.heh.projet_mastock.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    // Database instance
    private lateinit var db: MyDB
    // User object to store user information
    private var user: UserRecord = UserRecord(0, "", "", false, false)
    // Password fields
    private var pwd1: String = ""
    private var pwd2: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for the activity
        setContentView(R.layout.activity_register)

        // Initialize the database instance
        db = MyDB.getDB(this)

        // Set up listeners to update user and password fields as text changes
        findViewById<TextInputEditText>(R.id.ar_tiet_email).doOnTextChanged { text, start, before, count ->
            user.email = findViewById<TextInputEditText>(R.id.ar_tiet_email).text.toString()
            findViewById<TextInputLayout>(R.id.ar_til_email).error = null
        }
        findViewById<TextInputEditText>(R.id.ar_tiet_password).doOnTextChanged { text, start, before, count ->
            pwd1 = findViewById<TextInputEditText>(R.id.ar_tiet_password).text.toString()
            findViewById<TextInputLayout>(R.id.ar_til_password).error = null
        }
        findViewById<TextInputEditText>(R.id.ar_tiet_password_confirm).doOnTextChanged { text, start, before, count ->
            pwd2 = findViewById<TextInputEditText>(R.id.ar_tiet_password_confirm).text.toString()
            findViewById<TextInputLayout>(R.id.ar_til_password_confirm).error = null
        }

        // Set up click listener for the confirm button
        findViewById<Button>(R.id.ar_bp_confirm)
            .setOnClickListener {
                // Check data validity and save user if valid
                if (checkData()) {
                    try {
                        // Use coroutines for asynchronous database operations
                        lifecycleScope.launch {
                            // Check if the user already exists
                            if (check()) {
                                // Save the user to the database and finish the activity
                                saveUser()
                                finish()
                            }
                        }
                    } catch (error: Error) {
                        Log.i("Error creating account!", error.toString())
                    }
                }
            }

        // Set up click listener for the cancel button
        findViewById<Button>(R.id.ar_bp_cancel)
            .setOnClickListener {
                // Finish the activity
                finish()
            }
    }

    // Function to check the validity of entered data
    private fun checkData(): Boolean {
        val emailPattern = Pattern.compile("^[a-zA-Z0-9._-]+@(heh\\.be|std\\.heh\\.be)$")

        // Validate email format
        if (user.email.isNullOrBlank() || !emailPattern.matcher(user.email).matches()) {
            findViewById<TextInputLayout>(R.id.ar_til_email).error = "Invalid email!"
            return false
        }

        // Validate password format and matching
        if (pwd1.isNullOrBlank() || pwd2.isNullOrBlank() || pwd1 != pwd2 || pwd1.length < 4 || pwd2.length < 4) {
            findViewById<TextInputLayout>(R.id.ar_til_password).error = "Invalid password!"
            findViewById<TextInputLayout>(R.id.ar_til_password_confirm).error = "Invalid password!"
            return false
        }

        // Update the user object with the entered password
        user.pwd = pwd1

        return true
    }

    // Function to check if the user already exists in the database
    private suspend fun check(): Boolean {
        val temp = withContext(Dispatchers.IO) {
            val existingUser = db.userDao().getUser(user.email)
            existingUser == null
        }

        // Display a toast message if the user already exists
        if (!temp) {
            Toast.makeText(this, "Account ${user.email} already exists.", Toast.LENGTH_LONG).show()
        }
        return temp
    }

    // Function to save the user to the database
    private fun saveUser() {
        GlobalScope.launch {
            db.userDao().insertUser(user)
            runOnUiThread {
                Toast.makeText(applicationContext, "Account ${user.email} is created.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
