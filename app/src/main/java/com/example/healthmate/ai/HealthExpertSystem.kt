package com.example.healthmate.ai

data class HealthAdvice(
    val status: String,
    val message: String
)

class HealthExpertSystem {

    fun analyzeHealth(weight: Double, height: Double, steps: Int): HealthAdvice {
        val bmi = weight / (height * height)
        val bmiStatus = classifyBmi(bmi)
        val stepAdvice = buildStepAdvice(bmi, steps)
        val bmiAdvice = buildBmiAdvice(bmi)

        val status = "$bmiStatus | BMI %.1f".format(bmi)
        val message = "$bmiAdvice $stepAdvice"

        return HealthAdvice(status = status, message = message)
    }

    /* ── BMI classification ────────────────────────────────────────── */

    private fun classifyBmi(bmi: Double): String = when {
        bmi < 18.5 -> "Thiếu cân"
        bmi < 25.0 -> "Bình thường"
        bmi < 30.0 -> "Thừa cân"
        else        -> "Béo phì"
    }

    /* ── BMI-based advice (IF-THEN rules) ──────────────────────────── */

    private fun buildBmiAdvice(bmi: Double): String = when {
        bmi < 18.5 ->
            "Bạn hơi gầy đó, ăn uống đầy đủ dinh dưỡng vào nhé! 🥗"
        bmi < 25.0 ->
            "BMI ổn rồi, duy trì lối sống lành mạnh nha! 💪"
        bmi < 27.0 ->
            "Bạn hơi thừa cân, ráng đi đủ 8000 bước nhé! 🔥"
        bmi < 30.0 ->
            "Thừa cân rồi đấy, cần vận động nhiều hơn và ăn uống điều độ! 🏃"
        else ->
            "BMI cao quá, nên gặp bác sĩ tư vấn thêm nha! 🩺"
    }

    /* ── Step-based advice (IF-THEN rules) ─────────────────────────── */

    private fun buildStepAdvice(bmi: Double, steps: Int): String = when {
        steps < 3000 && bmi >= 25.0 ->
            "Hôm nay mới đi ${steps} bước, cố gắng vận động thêm đi nè!"
        steps < 3000 ->
            "Mới ${steps} bước thôi, tranh thủ đi thêm chút nữa nhé! 🚶"
        steps < 5000 && bmi >= 25.0 ->
            "${steps} bước rồi, thêm chút nữa là đạt mục tiêu!"
        steps < 5000 ->
            "${steps} bước, cũng tạm rồi nhưng ráng lên 8000 nhé!"
        steps < 8000 && bmi >= 25.0 ->
            "${steps} bước tốt lắm, ráng thêm ${8000 - steps} bước nữa nha! 💪"
        steps < 8000 ->
            "${steps} bước rồi, gần đạt mục tiêu 8000 bước rồi! 🎯"
        steps < 10000 ->
            "${steps} bước, xuất sắc! Đã đạt mục tiêu 8000 bước! 🏆"
        else ->
            "${steps} bước luôn?! Quá đỉnh, bạn là nhà vô địch! 🌟"
    }
}
