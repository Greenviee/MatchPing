// src/main/java/com/example/matchping/TFLiteClassifier.kt
package com.example.matchping

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileNotFoundException
import java.nio.channels.FileChannel

class TFLiteClassifier(private val context: Context) {

    companion object {
        private const val MODEL_NAME = "model.tflite"
        private const val TAG = "TFLiteClassifier"
        // 9개 태그
        private val TAGS = listOf(
            "왼손잡이","쉐이크","펜홀더",
            "너클 서브","전진 서브","커트 서브",
            "드라이브","속공","수비형"
        )
    }

    private var interpreter: Interpreter? = null

    init {
        try {
            val afd = context.assets.openFd(MODEL_NAME)
            val mapped = afd.createInputStream().channel.map(
                FileChannel.MapMode.READ_ONLY,
                afd.startOffset,
                afd.declaredLength
            )
            val options = Interpreter.Options().apply { setNumThreads(4) }
            interpreter = Interpreter(mapped, options)
            Log.d(TAG, "✅ 모델 로드 성공: assets/$MODEL_NAME")
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "❌ $MODEL_NAME 을 찾을 수 없습니다.", e)
        } catch (e: Exception) {
            Log.e(TAG, "❌ TFLite 인터프리터 초기화 오류", e)
        }
    }

    /**
     * 추론 수행
     * @param opponentTags  상대 태그 리스트
     * @param formula       prior 공식 값 ([0..1])
     * @return 예측 승률 [0..1], 인터프리터가 없으면 0.5
     */
    fun predict(
        opponentTags: List<String>,
        formula: Float
    ): Float {
        val interp = interpreter ?: return 0.5f

        // (1) 태그 one-hot [1×9]
        val tagVec = FloatArray(TAGS.size) { i ->
            if (TAGS[i] in opponentTags) 1f else 0f
        }
        val inputTags = arrayOf(tagVec)                 // shape=[1][9]

        // (2) prior 공식 [1×1]
        val inputFormula = arrayOf(floatArrayOf(formula))  // shape=[1][1]

        // (3) 출력 버퍼
        val output = Array(1) { FloatArray(1) }           // shape=[1][1]

        // (4) run
        interp.runForMultipleInputsOutputs(
            arrayOf<Any>(inputTags, inputFormula),
            mapOf(0 to output)
        )

        return output[0][0].coerceIn(0f,1f)
    }
}
