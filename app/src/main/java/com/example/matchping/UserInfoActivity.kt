package com.example.matchping

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1) IP 정보 모델
data class IpInfo(
    val city: String,
    val country: String
)

// 2) Retrofit 인터페이스
interface IpApi {
    // 발급받은 토큰을 넣어주세요
    @GET("json?token=26dd151f2c40cf")
    suspend fun getInfo(): IpInfo
}

class UserInfoActivity : AppCompatActivity() {

    private lateinit var imageProfile: ImageView
    private lateinit var textName: TextView
    private lateinit var textId: TextView
    private lateinit var textPhone: TextView
    private lateinit var textUnit: TextView
    private lateinit var textLocation: TextView    // 새로 추가
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonEdit: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Retrofit 인스턴스
    private val ipApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://ipinfo.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IpApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        imageProfile  = findViewById(R.id.imageProfile)
        textName      = findViewById(R.id.textName)
        textId        = findViewById(R.id.textId)
        textPhone     = findViewById(R.id.textPhone)
        textUnit      = findViewById(R.id.textUnit)
        textLocation  = findViewById(R.id.textLocation)  // 연결
        buttonBack    = findViewById(R.id.buttonBack)
        buttonEdit    = findViewById(R.id.buttonEdit)

        val uid = auth.currentUser?.uid ?: return

        // Firestore에서 사용자 정보 불러오기
        db.collection("Users").document(uid).get().addOnSuccessListener { doc ->
            val user = doc.toObject(User::class.java)
            if (user != null) {
                textName.text  = user.name
                textId.text    = "아이디: ${user.id}"
                textPhone.text = "전화번호: ${user.phone}"
                textUnit.text  = "부수: ${user.unit}"
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

        // IP 기반 위치 정보 로드
        lifecycleScope.launch {
            try {
                val info = ipApi.getInfo()
                textLocation.text = "위치: ${info.city}, ${info.country}"
            } catch (e: Exception) {
                textLocation.text = "위치 정보 불러오기 실패"
            }
        }

        buttonBack.setOnClickListener { finish() }
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
