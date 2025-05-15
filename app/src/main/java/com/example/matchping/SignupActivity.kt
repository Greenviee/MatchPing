package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        val spinnerLevel = findViewById<Spinner>(R.id.spinnerLevel)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        // 예시 레벨 데이터
        val levels = arrayOf("초급", "중급", "상급")
        spinnerLevel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, levels)

        buttonRegister.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val email = editTextId.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()
            val level = spinnerLevel.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    // 사용자 정보 DB에 저장
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val userInfo = mapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "level" to level
                    )
                    database.child("users").child(uid).setValue(userInfo)
                        .addOnSuccessListener {
                            Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "회원정보 저장 실패", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "회원가입 실패: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
