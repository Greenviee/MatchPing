package com.example.matchping

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditUserInfoActivity : AppCompatActivity() {

    private val PICK_IMAGE = 1
    private var imageUri: Uri? = null

    private lateinit var imageProfileEdit: ImageView
    private lateinit var editTextNameEdit: EditText
    private lateinit var editTextIdEdit: EditText
    private lateinit var editTextPhoneEdit: EditText
    private lateinit var editTextUnitEdit: EditText
    private lateinit var buttonSaveEdit: Button
    private lateinit var buttonBackEdit: ImageButton

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_info)

        imageProfileEdit = findViewById(R.id.imageProfileEdit)
        editTextNameEdit = findViewById(R.id.editTextNameEdit)
        editTextIdEdit = findViewById(R.id.editTextIdEdit)
        editTextPhoneEdit = findViewById(R.id.editTextPhoneEdit)
        editTextUnitEdit = findViewById(R.id.editTextUnitEdit)
        buttonSaveEdit = findViewById(R.id.buttonSaveEdit)
        buttonBackEdit = findViewById(R.id.buttonBackEdit)

        val uid = auth.currentUser?.uid ?: return

        // 뒤로가기 버튼: 단순 finish
        buttonBackEdit.setOnClickListener {
            finish()
        }

        // Firestore에서 기존 사용자 정보 가져오기
        db.collection("Users").document(uid).get().addOnSuccessListener { doc ->
            val user = doc.toObject(User::class.java)
            user?.let {
                editTextNameEdit.setText(it.name)
                editTextIdEdit.setText(it.id)
                editTextPhoneEdit.setText(it.phone)
                editTextUnitEdit.setText(it.unit)
                if (!it.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(it.profileImageUrl)
                        .error(android.R.drawable.sym_def_app_icon)
                        .into(imageProfileEdit)
                } else {
                    imageProfileEdit.setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }
        }

        // 프로필 사진 클릭시 갤러리 열기
        imageProfileEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        // 저장 버튼
        buttonSaveEdit.setOnClickListener {
            val name = editTextNameEdit.text.toString().trim()
            val phone = editTextPhoneEdit.text.toString().trim()
            val unit = editTextUnitEdit.text.toString().trim()
            if (name.isEmpty() || phone.isEmpty() || unit.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri != null) {
                // 새 프로필 이미지 Storage 업로드
                val ref = storage.reference.child("profile_images/$uid.jpg")
                ref.putFile(imageUri!!)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            throw task.exception ?: Exception("사진 업로드 실패")
                        }
                        ref.downloadUrl
                    }
                    .addOnSuccessListener { downloadUri ->
                        saveUserInfo(uid, name, phone, unit, downloadUri.toString())
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "프로필 사진 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // 기존 프로필 이미지 유지
                db.collection("Users").document(uid).get().addOnSuccessListener { doc ->
                    val user = doc.toObject(User::class.java)
                    val profileUrl = user?.profileImageUrl ?: ""
                    saveUserInfo(uid, name, phone, unit, profileUrl)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            // 시스템 아이콘 또는 선택 이미지 미리보기
            Glide.with(this)
                .load(imageUri)
                .error(android.R.drawable.sym_def_app_icon)
                .into(imageProfileEdit)
        }
    }

    private fun saveUserInfo(uid: String, name: String, phone: String, unit: String, imageUrl: String) {
        val updates = mapOf(
            "name" to name,
            "phone" to phone,
            "unit" to unit,
            "profileImageUrl" to imageUrl
        )
        db.collection("Users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "정보 저장 실패", Toast.LENGTH_SHORT).show()
            }
    }
}
