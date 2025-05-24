package com.example.matchping

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 로그인된 사용자의 Firestore 문서에 fcmToken 필드로 저장
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(uid)
                .update("fcmToken", token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // 포그라운드 수신 시 처리 (알림 띄우기 or 화면 이동)
        message.notification?.let {
            // 예시: system notification 으로 띄우거나
            // 클릭 시 MatchRequestActivity 로 인텐트 연결
        }
    }
}
