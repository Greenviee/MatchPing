package com.example.matchping

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val REQUEST_CONTACT = 1001

    private lateinit var listViewFriends: ListView
    private lateinit var friendAdapter: ArrayAdapter<String>
    private lateinit var appDatabase: AppDatabase
    private lateinit var friendDao: FriendDao

    // 반드시 List<Friend>로 유지해야 position 참조 시 정보 전달 가능!
    private var friendList: List<Friend> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 로그인 안 된 경우 진입 방지
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_friend)

        val editTextFriendId = findViewById<EditText>(R.id.editTextFriendId)
        val buttonAddFriendById = findViewById<Button>(R.id.buttonAddFriendById)
        val buttonSyncContacts = findViewById<ImageButton>(R.id.buttonSyncContacts)
        val buttonBackFriend = findViewById<ImageButton>(R.id.buttonBackFriend)
        listViewFriends = findViewById(R.id.listViewFriends)

        appDatabase = AppDatabase.getDatabase(this)
        friendDao = appDatabase.friendDao()

        buttonBackFriend.setOnClickListener { finish() }

        buttonAddFriendById.setOnClickListener {
            val friendId = editTextFriendId.text.toString().trim()
            if (friendId.isNotEmpty()) {
                ensureFriendsFieldExists {
                    addFriendById(friendId)
                }
            }
        }

        buttonSyncContacts.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACT)
            } else {
                ensureFriendsFieldExists {
                    syncContacts()
                }
            }
        }

        ensureFriendsFieldExists {
            loadFriends()
        }
    }

    // friends 필드 자동 생성(없으면 빈 배열로)
    private fun ensureFriendsFieldExists(onReady: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(uid)
        userRef.get().addOnSuccessListener { doc ->
            if (!doc.contains("friends")) {
                userRef.update("friends", listOf<String>())
                    .addOnSuccessListener { onReady() }
                    .addOnFailureListener { onReady() }
            } else {
                onReady()
            }
        }.addOnFailureListener { onReady() }
    }

    // Firestore + Room에서 내 친구 목록 불러오기 + 클릭 시 상세 이동
    private fun loadFriends() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Users").document(uid).get().addOnSuccessListener { doc ->
            val friends = doc.get("friends") as? List<String> ?: emptyList()
            if (friends.isNotEmpty()) {
                db.collection("Users").whereIn(com.google.firebase.firestore.FieldPath.documentId(), friends).get()
                    .addOnSuccessListener { snapshots ->
                        // 1. Friend 객체 리스트 유지
                        friendList = snapshots.documents.map { userDoc ->
                            Friend(
                                uid = userDoc.id,
                                name = userDoc.getString("name") ?: "",
                                phone = userDoc.getString("phone") ?: "",
                                id = userDoc.getString("id") ?: "",
                                profileImageUrl = userDoc.getString("profileImageUrl")
                            )
                        }
                        // 2. 내부 DB 저장
                        lifecycleScope.launch(Dispatchers.IO) {
                            friendList.forEach { friendDao.insert(it) }
                        }
                        // 3. 이름 리스트로 어댑터 연결
                        val friendNames = friendList.map { it.name + " (" + it.id + ")" }
                        friendAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, friendNames)
                        listViewFriends.adapter = friendAdapter

                        // 4. 클릭 리스너에서 상세로 이동 (항상 어댑터 세팅 후 재등록!)
                        listViewFriends.setOnItemClickListener { _, _, position, _ ->
                            val selectedFriend = friendList[position]
                            val intent = Intent(this, FriendDetailActivity::class.java).apply {
                                putExtra("friendUid", selectedFriend.uid)
                                putExtra("friendName", selectedFriend.name)
                                putExtra("friendId", selectedFriend.id)
                                // 부수(unit)도 Firestore에 있으면 같이 넘기기!
                                val unitIdx = snapshots.documents[position].getString("unit") ?: ""
                                putExtra("friendUnit", unitIdx)
                            }
                            startActivity(intent)
                        }
                    }
            } else {
                friendAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf("친구 없음"))
                listViewFriends.adapter = friendAdapter
                // 친구 없으면 클릭 리스너 등록 X (옵션)
            }
        }
    }

    // 아이디로 친구 추가
    private fun addFriendById(friendId: String) {
        val myUid = auth.currentUser?.uid ?: return
        db.collection("Users").whereEqualTo("id", friendId).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val friendDoc = result.documents[0]
                    val friendUid = friendDoc.id
                    db.collection("Users").document(myUid)
                        .update("friends", FieldValue.arrayUnion(friendUid))
                        .addOnSuccessListener {
                            // 내부 DB 저장
                            lifecycleScope.launch(Dispatchers.IO) {
                                friendDao.insert(
                                    Friend(
                                        uid = friendUid,
                                        name = friendDoc.getString("name") ?: "",
                                        phone = friendDoc.getString("phone") ?: "",
                                        id = friendDoc.getString("id") ?: "",
                                        profileImageUrl = friendDoc.getString("profileImageUrl")
                                    )
                                )
                            }
                            Toast.makeText(this, "친구 추가 성공!", Toast.LENGTH_SHORT).show()
                            loadFriends()
                        }
                } else {
                    Toast.makeText(this, "해당 아이디의 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 주소록 연동
    private fun syncContacts() {
        val contacts = mutableSetOf<String>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )
        if (cursor != null) {
            val idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val phone = cursor.getString(idx)
                val normalized = phone.replace("[^0-9]".toRegex(), "")
                contacts.add(normalized)
            }
            cursor.close()
        }
        if (contacts.isEmpty()) {
            Toast.makeText(this, "주소록에 등록된 번호가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        // Users에서 phone이 contacts에 있는 유저 찾기 (최대 10개씩 쿼리 필요)
        val contactList = contacts.toList()
        val myUid = auth.currentUser?.uid ?: return
        val batch = contactList.chunked(10)
        for (phones in batch) {
            db.collection("Users").whereIn("phone", phones).get()
                .addOnSuccessListener { result ->
                    result.documents.forEach { userDoc ->
                        val friendUid = userDoc.id
                        if (friendUid != myUid) {
                            db.collection("Users").document(myUid)
                                .update("friends", FieldValue.arrayUnion(friendUid))
                                .addOnSuccessListener {
                                    // 내부DB 저장
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        friendDao.insert(
                                            Friend(
                                                uid = friendUid,
                                                name = userDoc.getString("name") ?: "",
                                                phone = userDoc.getString("phone") ?: "",
                                                id = userDoc.getString("id") ?: "",
                                                profileImageUrl = userDoc.getString("profileImageUrl")
                                            )
                                        )
                                    }
                                }
                        }
                    }
                    loadFriends()
                    Toast.makeText(this, "주소록 기반 친구 동기화 완료!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CONTACT && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ensureFriendsFieldExists {
                syncContacts()
            }
        }
    }
}
