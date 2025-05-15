package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonSignup = findViewById<Button>(R.id.buttonSignup)
        val buttonGuest = findViewById<Button>(R.id.buttonGuest)

        buttonLogin.setOnClickListener {
            val email = editTextId.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // 로그인 성공: activity_main 이동
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "로그인 실패: 아이디 또는 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
                }
        }

        buttonSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        buttonGuest.setOnClickListener {
            startActivity(Intent(this, ScoreboardActivity::class.java))
            finish()
        }
    }
}
