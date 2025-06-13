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
        myAbility: FloatArray,      // size=5
        myUnit: Int,                // size=1
        oppUnit: Int,               // size=1
        oppTags: List<String>       // size=9
    ): Float {
        // slot 0: 태그 9
        val tagVec = FloatArray(9) { i -> if (TAGS[i] in oppTags) 1f else 0f }
        val in0 = arrayOf(tagVec)                     // [1][9]

        // slot 1: 내 부수 1
        val in1 = arrayOf(floatArrayOf(myUnit.toFloat()))  // [1][1]

        // slot 2: 상대 부수 1
        val in2 = arrayOf(floatArrayOf(oppUnit.toFloat())) // [1][1]

        // slot 3: 능력치 5
        val in3 = arrayOf(myAbility)                  // [1][5]

        val inputs  = arrayOf<Any>(in0, in1, in2, in3)
        val outputs = mutableMapOf<Int, Any>(0 to Array(1) { FloatArray(1) })

        interpreter!!.runForMultipleInputsOutputs(inputs, outputs)
        return (outputs[0] as Array<FloatArray>)[0][0].coerceIn(0f,1f)
    }


}
