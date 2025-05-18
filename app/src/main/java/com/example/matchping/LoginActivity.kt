package com.example.matchping

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val PREFS_NAME = "AutoLoginPrefs"
    private val AUTO_LOGIN_KEY = "autoLoginChecked"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonSignup = findViewById<Button>(R.id.buttonSignup)
        val buttonGuest = findViewById<Button>(R.id.buttonGuest)
        val checkBoxAutoLogin = findViewById<CheckBox>(R.id.checkBoxAutoLogin)

        // 이전에 체크한 값이 있으면 체크박스에 표시
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        checkBoxAutoLogin.isChecked = prefs.getBoolean(AUTO_LOGIN_KEY, false)

        // 로그인 버튼 클릭
        buttonLogin.setOnClickListener {
            val id = editTextId.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val email = "$id@matchping.com"

            // 자동로그인 체크값 저장
            prefs.edit().putBoolean(AUTO_LOGIN_KEY, checkBoxAutoLogin.isChecked).apply()

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "로그인 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        buttonSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        buttonGuest.setOnClickListener {
            startActivity(Intent(this, GameMatchActivity::class.java))
            finish()
        }
    }

    // 자동로그인 체크한 경우에만 실행
    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val autoLoginChecked = prefs.getBoolean(AUTO_LOGIN_KEY, false)
        if (autoLoginChecked && auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
