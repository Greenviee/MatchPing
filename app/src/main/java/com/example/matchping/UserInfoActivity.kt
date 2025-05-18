package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserInfoActivity : AppCompatActivity() {

    private lateinit var imageProfile: ImageView
    private lateinit var textName: TextView
    private lateinit var textId: TextView
    private lateinit var textPhone: TextView
    private lateinit var textUnit: TextView
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonEdit: ImageButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        imageProfile = findViewById(R.id.imageProfile)
        textName = findViewById(R.id.textName)
        textId = findViewById(R.id.textId)
        textPhone = findViewById(R.id.textPhone)
        textUnit = findViewById(R.id.textUnit)
        buttonBack = findViewById(R.id.buttonBack)
        buttonEdit = findViewById(R.id.buttonEdit)

        val uid = auth.currentUser?.uid ?: return

        // Firestore에서 사용자 정보 불러오기
        db.collection("Users").document(uid).get().addOnSuccessListener { doc ->
            val user = doc.toObject(User::class.java)
            if (user != null) {
                textName.text = user.name
                textId.text = "아이디: ${user.id}"
                textPhone.text = "전화번호: ${user.phone}"
                textUnit.text = "부수: ${user.unit}"
                if (!user.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(user.profileImageUrl)
                        .error(android.R.drawable.sym_def_app_icon)
                        .into(imageProfile)
                } else {
                    imageProfile.setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }
        }

        // 뒤로가기: 현재 액티비티 종료 (메인 or 이전 화면으로 이동)
        buttonBack.setOnClickListener {
            finish()
        }

        // 편집 버튼: EditUserInfoActivity로 이동
        buttonEdit.setOnClickListener {
            startActivity(Intent(this, EditUserInfoActivity::class.java))
        }

        val buttonLogout = findViewById<Button>(R.id.buttonLogout)
        buttonLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
