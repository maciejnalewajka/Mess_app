package pl.example.messenger.messenger.login_register

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import pl.example.messenger.messenger.messages.LatestMessagesActivity
import com.example.messenger.messenger.R
import pl.example.messenger.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private var selectedPhoto: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            rejestracja()
        }

        already_textview_register.setOnClickListener {
            finish()
        }

        photo_image_register.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }

        constraint_layout_register.setOnClickListener {
            hideKeyboard()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)     {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhoto = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhoto)
            photo_image_register.setImageBitmap(bitmap)
            add_textView_register.text = ""
        }
    }

    private fun rejestracja() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        val password2 = password_edittext_register2.text.toString()
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Wpisz e-mail i hasło!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 8) {
            Toast.makeText(this, "Hasła musi mieć 8 znaków!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != password2){
            Toast.makeText(this, "Hasła są różne!", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Zaczekaj...", Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    uploadPhotoToFirebase()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Włączony internet ?", Toast.LENGTH_SHORT).show()
                }
    }

    private fun uploadPhotoToFirebase() {
        if (selectedPhoto == null) {
            Toast.makeText(this, "Nie wybrano zdjęcia!", Toast.LENGTH_SHORT).show()
            return
    }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/image/$filename")
        ref.putFile(selectedPhoto!!)
                .addOnSuccessListener {
                    Toast.makeText(this, "Gotowe!", Toast.LENGTH_SHORT).show()
                    ref.downloadUrl.addOnSuccessListener { it1 ->
                        saveUserOnDatabase(it1.toString())
                    }
                }
    }

    private fun saveUserOnDatabase(imageProfileUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, username_edittext_register.text.toString(), imageProfileUrl)
        ref.setValue(user)
                .addOnSuccessListener {
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}