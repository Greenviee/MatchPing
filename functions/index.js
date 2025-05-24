const functions = require("firebase-functions");
const admin     = require("firebase-admin");
admin.initializeApp();

exports.onMatchRequest = functions.firestore
  .document("matchRequests/{reqId}")
  .onCreate(async (snap, ctx) => {
    const data    = snap.data();
    const fromUid = data.fromUid;
    const toUid   = data.toUid;
    if (!fromUid || !toUid) {
      console.log("Missing fromUid or toUid:", data);
      return null;
    }

    // 보낸 사람 이름 조회
    const fromDoc = await admin.firestore().doc(`Users/${fromUid}`).get();
    const fromName = fromDoc.exists && fromDoc.data().name
      ? fromDoc.data().name
      : "친구";

    // 받을 사람 FCM 토큰 조회
    const toDoc = await admin.firestore().doc(`Users/${toUid}`).get();
    const token = toDoc.exists && toDoc.data().fcmToken;
    if (!token) {
      console.log("No fcmToken for toUid:", toUid);
      return null;
    }

    // 알림 payload
    const payload = {
      notification: {
        title: "매치 요청이 도착했습니다!",
        body: `${fromName}님이 대전을 요청했어요.`,
      },
      data: {
        type: "MATCH_REQUEST",
        fromUid: fromUid,
      }
    };

    // 전송
    try {
      await admin.messaging().sendToDevice(token, payload);
      console.log("Push sent to", toUid);
    } catch (err) {
      console.error("Error sending push:", err);
    }
    return null;
  });
