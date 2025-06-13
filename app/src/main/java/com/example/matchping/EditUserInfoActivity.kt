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
import java.io.File
import java.io.FileOutputStream

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

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_info)

        imageProfileEdit = findViewById(R.id.imageProfileEdit)
        editTextNameEdit = findViewById(R.id.editTextNameEdit)
        editTextIdEdit   = findViewById(R.id.editTextIdEdit)
        editTextPhoneEdit= findViewById(R.id.editTextPhoneEdit)
        editTextUnitEdit = findViewById(R.id.editTextUnitEdit)
        buttonSaveEdit   = findViewById(R.id.buttonSaveEdit)
        buttonBackEdit   = findViewById(R.id.buttonBackEdit)

        val uid = auth.currentUser?.uid ?: return

        // 뒤로가기
        buttonBackEdit.setOnClickListener { finish() }

        // 기존 정보 로드
        db.collection("Users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                doc.toObject(User::class.java)?.let { user ->
                    editTextNameEdit.setText(user.name)
                    editTextIdEdit.setText(user.id)
                    editTextPhoneEdit.setText(user.phone)
                    editTextUnitEdit.setText(user.unit)
                    // profileImageUrl 필드에 로컬 경로를 쓴다고 가정
                    val localPath = user.profileImageUrl
                    if (localPath.isNotEmpty()) {
                        Glide.with(this)
                            .load(File(localPath))
                            .error(android.R.drawable.sym_def_app_icon)
                            .into(imageProfileEdit)
                    } else {
                        imageProfileEdit.setImageResource(android.R.drawable.sym_def_app_icon)
                    }
                }
            }

        // 이미지 선택
        imageProfileEdit.setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_PICK).apply { type = "image/*" },
                PICK_IMAGE
            )
        }

        // 저장
        buttonSaveEdit.setOnClickListener {
            val name  = editTextNameEdit.text.toString().trim()
            val id    = editTextIdEdit.text.toString().trim()
            val phone = editTextPhoneEdit.text.toString().trim()
            val unit  = editTextUnitEdit.text.toString().trim()
            if (name.isEmpty() || id.isEmpty() || phone.isEmpty() || unit.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1) 사진이 선택되었다면 로컬에 복사
            val localPath = imageUri?.let { uri ->
                val filename = "profile_${uid}.jpg"
                val destFile = File(filesDir, filename)
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destFile.absolutePath
            } ?: ""  // 선택 없으면 빈 문자열

            // 2) Firestore에 사용자 정보만 업데이트
            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "id"   to id,
                "phone" to phone,
                "unit"  to unit
            )
            // 로컬 경로는 profileImageUrl 필드에 함께 저장해 둡니다
            if (localPath.isNotEmpty()) {
                updates["profileImageUrl"] = localPath
            }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            Glide.with(this)
                .load(imageUri)
                .error(android.R.drawable.sym_def_app_icon)
                .into(imageProfileEdit)
        }
    }
}
