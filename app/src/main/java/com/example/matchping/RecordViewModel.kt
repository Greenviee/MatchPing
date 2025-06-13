package com.example.matchping

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import java.util.Random

/**
 * 전적 데이터 로드·검색·더미 생성·능력치 계산·모델 예측을 담당하는 ViewModel
 */
class RecordViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        // 태그 목록
        val ALL_TAGS = listOf(
            "왼손잡이", "쉐이크", "펜홀더",
            "너클 서브", "전진 서브", "커트 서브",
            "드라이브", "속공", "수비형"
        )

        /** "선수부"=0, "1부"=1 … "8부"=8 */
        fun unitStringToInt(unit: String): Int = when (unit) {
            "선수부" -> 0; "1부" -> 1; "2부" -> 2; "3부" -> 3
            "4부" -> 4; "5부" -> 5; "6부" -> 6; "7부" -> 7; "8부" -> 8
            else -> 0
        }

        /** 부수 검색을 위한 목록 (추가) */
        val ALL_UNITS = listOf(
            "선수부", "1부", "2부", "3부",
            "4부", "5부", "6부", "7부", "8부"
        )
    }

    private val auth       = FirebaseAuth.getInstance()
    private val dao        = AppDatabase.getDatabase(app).matchResultDao()
    private val classifier = TFLiteClassifier(app)

    // 전체 전적 LiveData
    private val _matches = MutableLiveData<List<MatchResult>>()
    val matches: LiveData<List<MatchResult>> = _matches

    // 검색/필터 결과 LiveData
    private val _searchResults = MutableLiveData<List<MatchResult>>()
    val searchResults: LiveData<List<MatchResult>> = _searchResults

    /** 사용자별 최근 전적 로드 */
    fun loadRecent() {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = auth.currentUser?.uid ?: return@launch
            _matches.postValue(dao.getMatchesByUser(uid))
        }
    }

    /** 상대 이름으로 검색 */
    fun searchByName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchResults.postValue(dao.getMatchesByOpponent(name))
        }
    }

    /** 부수(unit)로 검색 */
    fun searchByUnit(unit: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _searchResults.postValue(dao.getMatchesByUnit(uid, unit))
        }
    }

    /** 태그로 필터링 */
    fun filterByTag(tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = auth.currentUser?.uid ?: return@launch
            val all = dao.getMatchesByUser(uid)
            _searchResults.postValue(all.filter { it.tags.contains(tag) })
        }
    }

    /**
     * 테스트용 더미 전적 생성
     */
    fun generateDummyMatches(count: Int = 500, myRank: Int) = viewModelScope.launch(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@launch
        val rnd = Random(System.currentTimeMillis())
        val units = listOf("선수부","1부","2부","3부","4부","5부","6부","7부","8부")

        // 1) 리스트에 더미를 모아서
        val dummyList = mutableListOf<MatchResult>()
        repeat(count) { idx ->
            // 1. 상대 부수 랜덤 선택
            val oppUnit = units[rnd.nextInt(units.size)]
            val oppRank = unitStringToInt(oppUnit)

            // 2. prior 확률 계산
            val diff  = oppRank - myRank
            val n     = abs(diff) + 1
            val pHigh = ((2.0.pow(n.toDouble()) - 1) / 2.0.pow(n.toDouble())).toFloat()
            val prior = when {
                diff == 0   -> 0.5f
                diff > 0    -> pHigh    // 내가 더 강할 때
                else        -> 1f - pHigh
            }

            // 3. 5판 3선승제 시뮬레이션
            var mySets = 0
            var opSets = 0
            while (mySets < 3 && opSets < 3) {
                if (rnd.nextFloat() < prior) mySets++ else opSets++
            }

            // 4. 랜덤 태그
            val tags = ALL_TAGS.shuffled(rnd)
                .take(rnd.nextInt(1, ALL_TAGS.size))

            // 5. MatchResult 객체 생성
            dummyList += MatchResult(
                myUid            = uid,
                opponentName     = "DummyOpponent${idx + 1}",
                opponentUnit     = oppUnit,
                date             = System.currentTimeMillis()
                        - rnd.nextLong(0, 365L * 24 * 60 * 60 * 1000),
                result           = if (mySets > opSets) "승" else "패",
                mySetScore       = mySets,
                opponentSetScore = opSets,
                detail           = "",      // 필요 시 세트별 기록을 포맷해서 넣어주세요
                tags             = tags
            )
        }

        // 2) DAO에 일괄 삽입
        dao.insertAll(dummyList)

        // 3) LiveData 한 번만 갱신
        val updated = dao.getMatchesByUser(uid)
        _matches.postValue(updated)
    }

    /** 능력치 계산 결과를 담을 데이터 클래스 */
    data class AbilityScores(
        val leftHandAdapt: Float,
        val penholdAdapt:  Float,
        val serveAdapt:    Float,
        val confidence:    Float,
        val overallWinRate: Float
    )

    /** 통계 기반 능력치 계산 */
    fun calculateAbilityScores(userRank: Int?): AbilityScores {
        val records = _matches.value ?: emptyList()
        val stats   = mutableMapOf<String, Pair<Int, Int>>()  // tag → (wins, total)

        // 각 태그별 승패 집계
        records.forEach { rec ->
            val won = rec.result == "승"
            rec.tags.intersect(ALL_TAGS).forEach { tag ->
                val (w, t) = stats[tag] ?: (0 to 0)
                stats[tag] = (w + if (won) 1 else 0) to (t + 1)
            }
        }
        val winRate = stats.mapValues { (_, wt) -> if (wt.second == 0) 0f else wt.first.toFloat() / wt.second }
        val totalWins  = stats.values.sumOf { it.first }
        val totalGames = stats.values.sumOf { it.second }
        val overall    = if (totalGames == 0) 0f else totalWins.toFloat() / totalGames

        val lt            = stats["왼손잡이"]?.second ?: 0
        val lw            = stats["왼손잡이"]?.first  ?: 0
        val leftRate      = if (lt == 0) overall else lw.toFloat() / lt
        val nonLeftRate   = if (totalGames - lt == 0) overall else (totalWins - lw).toFloat() / (totalGames - lt)
        val leftHandScore = 1f - abs(leftRate - nonLeftRate)

        val shakeRate    = winRate["쉐이크"]  ?: overall
        val penholdRate  = winRate["펜홀더"] ?: overall
        val penholdScore = 1f - abs(shakeRate - penholdRate)

        val serveTags   = listOf("너클 서브","전진 서브","커트 서브")
        val serveRates  = serveTags.map { winRate[it] ?: overall }
        val serveScore  = 1f - ((serveRates.maxOrNull() ?: overall) - (serveRates.minOrNull() ?: overall))

        val higherStats = records
            .filter { userRank != null && unitStringToInt(it.opponentUnit) > userRank }
            .fold(0 to 0) { acc, rec ->
                val (w, t) = acc
                (w + if (rec.result == "승") 1 else 0) to (t + 1)
            }
        val confidence = if (higherStats.second == 0) overall else higherStats.first.toFloat() / higherStats.second

        return AbilityScores(
            leftHandAdapt   = leftHandScore.coerceIn(0f, 1f),
            penholdAdapt    = penholdScore.coerceIn(0f, 1f),
            serveAdapt      = serveScore.coerceIn(0f, 1f),
            confidence      = confidence.coerceIn(0f, 1f),
            overallWinRate  = overall.coerceIn(0f, 1f)
        )
    }

    /**
     * 머신러닝 기반 승률 예측 (내 능력치 5, 내 부수, 상대 부수, 상대 태그 9개)
     * - 기존 predictWithTFLite 유지, 내부만 수정
     */
    fun predictWithTFLite(
        tags: List<String>,      // 상대 태그 리스트
        myRank: Int,             // 내 부수 (정수)
        oppRank: Int             // 상대 부수 (정수)
    ): Float {
        val scores = calculateAbilityScores(myRank)
        val abilityArray = floatArrayOf(
            scores.leftHandAdapt,
            scores.penholdAdapt,
            scores.serveAdapt,
            scores.confidence,
            scores.overallWinRate
        )
        // 입력: 내 능력치(5), 내 부수(1), 상대 부수(1), 상대 태그(9) → 총 16개
        return classifier.predict(
            abilityArray,
            myRank,
            oppRank,
            tags
        )
    }
}
