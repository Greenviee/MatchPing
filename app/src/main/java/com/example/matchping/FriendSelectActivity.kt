package com.example.matchping

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendSelectActivity : AppCompatActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db   by lazy { FirebaseFirestore.getInstance() }

    private lateinit var listViewFriends: ListView
    private lateinit var buttonInvite: Button

    private var friendList: List<Friend> = emptyList()
    private var selectedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_select)

        listViewFriends = findViewById(R.id.listViewFriends)
        buttonInvite    = findViewById(R.id.buttonInvite)

        // 친구 목록 불러오기
        loadFriends()

        listViewFriends.choiceMode = ListView.CHOICE_MODE_SINGLE
        listViewFriends.setOnItemClickListener { _, _, position, _ ->
            selectedPosition = position
            listViewFriends.setItemChecked(position, true)
        }

        buttonInvite.setOnClickListener {
            if (selectedPosition == -1) {
                Toast.makeText(this, "친구를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val toFriend = friendList[selectedPosition]
            val fromUid  = auth.currentUser?.uid
            if (fromUid == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firestore에 matchRequests 문서 생성
            val req = mapOf(
                "fromUid"   to fromUid,
                "toUid"     to toFriend.uid,
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("matchRequests")
                .add(req)
                .addOnSuccessListener {
                    Toast.makeText(this,
                        "${toFriend.name}님에게 대전 요청을 보냈습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this,
                        "대전 요청 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun loadFriends() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Users").document(uid)
            .get()
            .addOnSuccessListener { userDoc ->
                // "friends" 필드는 Friend UID 리스트라 가정
                val friends = userDoc.get("friends") as? List<String> ?: emptyList()
                if (friends.isEmpty()) {
                    listViewFriends.adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        listOf("등록된 친구가 없습니다.")
                    )
                    return@addOnSuccessListener
                }

                // Firestore에서 실제 친구 정보 로드
                db.collection("Users")
                    .whereIn(FieldPath.documentId(), friends)
                    .get()
                    .addOnSuccessListener { snaps ->
                        friendList = snaps.documents.map { f ->
                            Friend(
                                uid = f.id,
                                name = f.getString("name") ?: "",
                                id   = f.getString("id") ?: "",
                                phone = f.getString("phone") ?: "",
                                profileImageUrl = f.getString("profileImageUrl")
                            )
                        }
                        val names = friendList.map { it.name + " (" + it.id + ")" }
                        listViewFriends.adapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_single_choice,
                            names
                        )
                        listViewFriends.choiceMode = ListView.CHOICE_MODE_SINGLE
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this,
                            "친구 불러오기 실패: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    "내 정보 불러오기 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
