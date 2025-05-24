package com.example.matchping

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private val unitList = listOf("선수부", "1부", "2부", "3부", "4부", "5부", "6부", "7부", "8부")
    private lateinit var spinnerUnit: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        spinnerUnit = findViewById(R.id.spinnerUnit)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        // Spinner(드롭다운) 부수 항목 연결
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unitList)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = unitAdapter

        buttonRegister.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val id = editTextId.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()
            val unit = spinnerUnit.selectedItem.toString()

            if (name.isEmpty() || id.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = "$id@matchping.com"
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = User(
                        uid = result.user?.uid ?: "",
                        name = name,
                        id = id,
                        phone = phone,
                        unit = unit,
                        profileImageUrl = "" // 기본 프로필, 추후 수정 가능
                    )
                    db.collection("Users").document(user.uid).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "DB 등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
