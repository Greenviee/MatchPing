package com.example.matchping

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val PREFS = "AutoLoginPrefs"
    private val KEY_AUTO = "autoLoginChecked"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        val etId    = findViewById<EditText>(R.id.editTextId)
        val etPw    = findViewById<EditText>(R.id.editTextPassword)
        val cbAuto  = findViewById<CheckBox>(R.id.checkBoxAutoLogin)
        val btnLog  = findViewById<Button>(R.id.buttonLogin)
        val btnSign = findViewById<Button>(R.id.buttonSignup)

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        cbAuto.isChecked = prefs.getBoolean(KEY_AUTO, false)

        btnLog.setOnClickListener {
            val id = etId.text.toString().trim()
            val pw = etPw.text.toString().trim()
            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val email = "$id@matchping.com"
            prefs.edit().putBoolean(KEY_AUTO, cbAuto.isChecked).apply()

            auth.signInWithEmailAndPassword(email, pw)
                .addOnSuccessListener {
                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Login", "signin error", e)
                }
        }

        btnSign.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_AUTO, false) && auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
