package com.example.healthmate.ai

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * On-device ML classifier using a TFLite model trained in Colab.
 *
 * Input features: Age, Height(cm), Weight(kg), Steps
 * Output: 3-class softmax → 0=Yếu, 1=Trung bình, 2=Khỏe
 */
class FitnessClassifier(context: Context) {

    private var interpreter: Interpreter? = null

    // Normalization parameters from Colab training
    private val minValues = floatArrayOf(18f, 150f, 45f, 1002f)
    private val maxValues = floatArrayOf(59f, 189f, 99f, 14998f)

    init {
        try {
            interpreter = Interpreter(loadModelFile(context))
        } catch (e: Exception) {
            e.printStackTrace()
            interpreter = null
        }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fd = context.assets.openFd("fitness_model.tflite")
        val inputStream = FileInputStream(fd.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }

    /**
     * Classify fitness level from user metrics.
     * @return 0 (Yếu/Lười), 1 (Trung bình), 2 (Khỏe)
     */
    fun classifyFitness(age: Int, heightCm: Double, weightKg: Double, steps: Int): Int {
        val interp = interpreter ?: return 1 // default to "Trung bình" if model unavailable

        // Normalize inputs with clamping to [0, 1]
        val raw = floatArrayOf(
            age.toFloat(),
            heightCm.toFloat(),
            weightKg.toFloat(),
            steps.toFloat()
        )
        val scaled = FloatArray(4) { i ->
            val range = maxValues[i] - minValues[i]
            val normalized = (raw[i] - minValues[i]) / range
            normalized.coerceIn(0f, 1f)
        }

        // Prepare input: shape [1, 4]
        val input = arrayOf(scaled)

        // Prepare output: shape [1, 3] (3-class softmax)
        val output = Array(1) { FloatArray(3) }

        // Run inference
        interp.run(input, output)

        // Argmax — find index of max probability
        return output[0].indices.maxByOrNull { output[0][it] } ?: 1
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
