package pl.example.messenger.messenger.login_register

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.example.messenger.messenger.R
import pl.example.messenger.messenger.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()
            if (!email.equals(null) && !password.equals(null)) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val intent = Intent(this, LatestMessagesActivity::class.java)
                            startActivity(intent)
                        }
            }
        }

        constraint_layout.setOnClickListener {
            hideKeyboard()
        }

        back_textview_login.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {}

    fun Activity.hideKeyboard() {
        hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}