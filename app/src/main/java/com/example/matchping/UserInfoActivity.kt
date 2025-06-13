package com.example.matchping

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class UserInfoActivity : AppCompatActivity() {

    private lateinit var imageProfile: ImageView
    private lateinit var textName: TextView
    private lateinit var textId: TextView
    private lateinit var textPhone: TextView
    private lateinit var textUnit: TextView
    private lateinit var textLocation: TextView
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonEdit: Button
    private lateinit var buttonLogout: Button

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        // view binding
        imageProfile    = findViewById(R.id.imageProfile)
        textName        = findViewById(R.id.textName)
        textId          = findViewById(R.id.textId)
        textPhone       = findViewById(R.id.textPhone)
        textUnit        = findViewById(R.id.textUnit)
        textLocation    = findViewById(R.id.textLocation)
        buttonBack      = findViewById(R.id.buttonBack)
        buttonEdit      = findViewById(R.id.buttonEdit)
        buttonLogout    = findViewById(R.id.buttonLogout)

        val uid = auth.currentUser?.uid ?: run {
            finish()
            return
        }

        // 1) Load user info from Firestore
        db.collection("Users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                user?.let {
                    textName.text  = it.name
                    textId.text    = "아이디: ${it.id}"
                    textPhone.text = "전화번호: ${it.phone}"
                    textUnit.text  = "부수: ${it.unit}"
                    if (it.profileImageUrl.isNotBlank()) {
                        Glide.with(this)
                            .load(it.profileImageUrl)
                            .error(android.R.drawable.sym_def_app_icon)
                            .into(imageProfile)
                    } else {
                        imageProfile.setImageResource(android.R.drawable.sym_def_app_icon)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "사용자 정보 불러오기 실패", Toast.LENGTH_SHORT).show()
            }

        // 2) Setup location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermissionAndFetch()

        // 3) Button listeners
        buttonBack.setOnClickListener { finish() }

        buttonEdit.setOnClickListener {
            startActivity(Intent(this, EditUserInfoActivity::class.java))
        }

        buttonLogout.setOnClickListener {
            auth.signOut()
            Intent(this, LoginActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            finish()
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            fetchLastLocation()
        }
    }

    private fun fetchLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val list = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val city = list?.firstOrNull()?.locality ?: list?.firstOrNull()?.adminArea ?: "알 수 없음"
                    textLocation.text = "위치: $city"
                } else {
                    textLocation.text = "위치 정보를 가져올 수 없습니다"
                }
            }
            .addOnFailureListener {
                textLocation.text = "위치 정보 불러오기 실패"
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLastLocation()
        } else {
            textLocation.text = "위치 권한이 필요합니다"
        }
    }
}
