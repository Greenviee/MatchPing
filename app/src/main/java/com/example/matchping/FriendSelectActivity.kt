package com.example.matchping

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendSelectActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var listViewFriends: ListView
    private lateinit var buttonInvite: Button
    private var friendList: List<Friend> = emptyList()
    private var selectedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_select)

        listViewFriends = findViewById(R.id.listViewFriends)
        buttonInvite = findViewById(R.id.buttonInvite)

        loadFriends()

        listViewFriends.setOnItemClickListener { _, _, position, _ ->
            selectedPosition = position
            listViewFriends.setItemChecked(position, true)
        }

        buttonInvite.setOnClickListener {
            if (selectedPosition == -1) {
                Toast.makeText(this, "친구를 선택하세요.", Toast.LENGTH_SHORT).show()
            } else {
                val selectedFriend = friendList[selectedPosition]
                // TODO: FCM 푸시 전송 구현
                // 예시: sendFcmInvite(selectedFriend)
                Toast.makeText(this, "${selectedFriend.name}님에게 대전 요청 전송!", Toast.LENGTH_SHORT).show()
                // 실제 앱에서는 진행중 다이얼로그 띄우고, 수락 신호 수신시 다음 화면으로 이동
            }
        }
    }

    private fun loadFriends() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Users").document(uid).get().addOnSuccessListener { doc ->
            val friends = doc.get("friends") as? List<String> ?: emptyList()
            if (friends.isNotEmpty()) {
                db.collection("Users").whereIn(com.google.firebase.firestore.FieldPath.documentId(), friends).get()
                    .addOnSuccessListener { snapshots ->
                        friendList = snapshots.documents.map { userDoc ->
                            Friend(
                                uid = userDoc.id,
                                name = userDoc.getString("name") ?: "",
                                phone = userDoc.getString("phone") ?: "",
                                id = userDoc.getString("id") ?: "",
                                profileImageUrl = userDoc.getString("profileImageUrl")
                            )
                        }
                        val friendNames = friendList.map { it.name + " (" + it.id + ")" }
                        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, friendNames)
                        listViewFriends.adapter = adapter
                        listViewFriends.choiceMode = ListView.CHOICE_MODE_SINGLE
                    }
            } else {
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf("친구 없음"))
                listViewFriends.adapter = adapter
            }
        }
    }
}
