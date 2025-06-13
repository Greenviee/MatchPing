package com.example.matchping

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendDetailActivity : AppCompatActivity() {

    private lateinit var imageFriendProfile: ImageView
    private lateinit var textFriendName: TextView
    private lateinit var textFriendId: TextView
    private lateinit var textFriendUnit: TextView
    private lateinit var buttonTag: Button
    private lateinit var layoutTags: LinearLayout

    private lateinit var appDatabase: AppDatabase
    private lateinit var friendTagDao: FriendTagDao

    // 예시 태그 리스트(필요시 확장)
    private val tagList = RecordViewModel.ALL_TAGS

    // 현재 친구 uid (인텐트로 받음)
    private lateinit var friendUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_detail)

        imageFriendProfile = findViewById(R.id.imageFriendProfile)
        textFriendName = findViewById(R.id.textFriendName)
        textFriendId = findViewById(R.id.textFriendId)
        textFriendUnit = findViewById(R.id.textFriendUnit)
        buttonTag = findViewById(R.id.buttonTag)
        layoutTags = findViewById(R.id.layoutTags)

        appDatabase = AppDatabase.getDatabase(this)
        friendTagDao = appDatabase.friendTagDao()

        // 인텐트로 친구 정보 전달받기
        val friendName = intent.getStringExtra("friendName") ?: ""
        val friendId = intent.getStringExtra("friendId") ?: ""
        val friendUnit = intent.getStringExtra("friendUnit") ?: ""
        friendUid = intent.getStringExtra("friendUid") ?: ""
        val profileImageUrl = intent.getStringExtra("friendProfileUrl") ?: ""

        textFriendName.text = friendName
        textFriendId.text = "아이디: $friendId"
        textFriendUnit.text = "부수: $friendUnit"

        // Glide로 프로필 이미지 로드
        if (profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(imageFriendProfile)
        } else {
            imageFriendProfile.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        // 태그 버튼 누르면 태그 영역 토글
        buttonTag.setOnClickListener {
            layoutTags.visibility = if (layoutTags.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // 태그 필터 UI 생성 및 온/오프 반영
        refreshTagList()
    }

    private fun refreshTagList() {
        layoutTags.removeAllViews()
        lifecycleScope.launch(Dispatchers.IO) {
            // [여기서 항상 friendUid로 구분해서 조회]
            val tags = friendTagDao.getTagsForFriend(friendUid)
            val enabledMap = tags.associate { it.tag to it.enabled }
            runOnUiThread {
                tagList.forEach { tagName ->
                    val tagButton = ToggleButton(this@FriendDetailActivity)
                    tagButton.textOn = tagName
                    tagButton.textOff = tagName
                    tagButton.text = tagName
                    tagButton.isChecked = enabledMap[tagName] == true

                    // ON/OFF 색상 명확히
                    setTagToggleButtonColor(tagButton, tagButton.isChecked)

                    tagButton.setOnCheckedChangeListener { _, isChecked ->
                        setTagToggleButtonColor(tagButton, isChecked)
                        lifecycleScope.launch(Dispatchers.IO) {
                            // [저장할 때도 friendUid와 tag, enabled로!]
                            friendTagDao.insert(
                                FriendTag(friendUid = friendUid, tag = tagName, enabled = isChecked)
                            )
                        }
                    }
                    layoutTags.addView(tagButton)
                }
            }
        }
    }

    private fun setTagToggleButtonColor(toggle: ToggleButton, isChecked: Boolean) {
        toggle.setBackgroundColor(
            if (isChecked) 0xFF2196F3.toInt() else 0xFFE0E0E0.toInt()
        )
        toggle.setTextColor(
            if (isChecked) 0xFFFFFFFF.toInt() else 0xFF444444.toInt()
        )
    }
}
