package com.example.healthmate.ai

import kotlin.random.Random

/* ═══════════════════════════════════════════════════════════════════════
 *  THUẬT TOÁN DI TRUYỀN (GENETIC ALGORITHM) — Tối ưu lịch tập luyện
 * ═══════════════════════════════════════════════════════════════════════
 *
 *  Thuật toán di truyền là một kỹ thuật AI lấy cảm hứng từ quá trình
 *  tiến hóa tự nhiên. Mỗi "cá thể" trong quần thể là một lịch tập
 *  (danh sách bài tập). Qua nhiều thế hệ, các lịch tập tốt nhất
 *  được chọn, lai ghép và đột biến để tạo ra lịch tập tối ưu.
 *
 *  Các bước chính:
 *  1. KHỞI TẠO QUẦN THỂ (Initialization) — Tạo ngẫu nhiên N lịch tập
 *  2. ĐÁNH GIÁ ĐỘ THÍCH NGHỊ (Fitness) — Lịch nào gần mục tiêu calo nhất?
 *  3. CHỌN LỌC (Selection) — Chọn các lịch tốt nhất để "sinh sản"
 *  4. LAI GHÉP (Crossover) — Kết hợp hai lịch cha mẹ thành lịch con
 *  5. ĐỘT BIẾN (Mutation) — Thay đổi ngẫu nhiên một bài tập trong lịch
 *  6. Lặp lại từ bước 2 qua nhiều thế hệ (generations)
 * ═══════════════════════════════════════════════════════════════════════ */

/**
 * Đại diện cho một bài tập đơn lẻ trong hệ thống.
 *
 * @param id         Định danh duy nhất, khớp với ExerciseRepository
 * @param name       Tên bài tập bằng tiếng Việt (ví dụ: "Chạy bộ")
 * @param calories   Số calo ước tính đốt được trong một buổi tập
 * @param durationMin Thời lượng bài tập (phút)
 */
data class Exercise(
    val id: String,
    val name: String,
    val calories: Int,
    val durationMin: Int
)

/**
 * Kết quả trả về sau khi thuật toán di truyền chạy xong.
 *
 * @param exercises     Danh sách các bài tập được chọn
 * @param totalCalories Tổng calo của toàn bộ lịch tập
 */
data class WorkoutPlan(
    val exercises: List<Exercise>,
    val totalCalories: Int
)

/**
 * ═══ THUẬT TOÁN DI TRUYỀN ═══
 *
 * Lớp chính chứa toàn bộ logic của thuật toán di truyền.
 * Nhận vào [targetCalories] — mục tiêu calo mà người dùng muốn đạt được.
 * Trả về một [WorkoutPlan] tối ưu nhất sau nhiều thế hệ tiến hóa.
 */
class WorkoutGeneticAlgorithm(private val targetCalories: Int) {

    /* ─────────────────────────────────────────────────────────────────
     *  BƯỚC 0: KHO BÀI TẬP (Exercise Pool)
     * ─────────────────────────────────────────────────────────────────
     *  Đây là tập hợp tất cả các bài tập có sẵn mà thuật toán
     *  có thể chọn từ đó để xây dựng lịch tập.
     *  Mỗi bài tập có tên tiếng Việt và số calo ước tính.
     * ───────────────────────────────────────────────────────────────── */
    private val exercisePool = listOf(
        // Beginner
        Exercise("yoga",              "Yoga thư giãn",            150, 45),
        Exercise("stretch",           "Kéo giãn thả lỏng",        80, 15),
        Exercise("walking_meditation", "Thiền đi bộ",             100, 20),
        Exercise("light_cycling",     "Đạp xe nhẹ",               120, 25),
        // Intermediate
        Exercise("cardio",            "Cardio đốt cháy",          280, 30),
        Exercise("pilates",           "Pilates dẻo dai",          180, 35),
        Exercise("dance_fitness",     "Nhảy fitness",             220, 30),
        Exercise("swimming",          "Bơi lội",                  250, 30),
        // Advanced
        Exercise("hiit",              "HIIT cực mạnh",            320, 20),
        Exercise("strength",          "Tăng cơ bắp",              250, 40),
        Exercise("crossfit",          "CrossFit cực đỉnh",        350, 25),
        Exercise("boxing",            "Boxing tung đấm",          300, 20)
    )

    /* ── Các tham số của thuật toán di truyền ──────────────────────── */

    /** KÍCH THƯỚC QUẦN THỂ (Population Size):
     *  Số lượng cá thể (lịch tập) trong mỗi thế hệ.
     *  Quần thể lớn hơn → đa dạng hơn nhưng chậm hơn. */
    private val populationSize = 20

    /** SỐ THẾ HỆ (Generations):
     *  Số lần lặp lại quá trình tiến hóa.
     *  Nhiều thế hệ hơn → kết quả tốt hơn nhưng tốn thời gian hơn. */
    private val generations = 50

    /** TỶ LỆ ĐỘT BIẾN (Mutation Rate):
     *  Xác suất một bài tập trong lịch bị thay thế bằng bài tập khác.
     *  Đột biến giúp duy trì sự đa dạng, tránh bị "kẹt" ở giải pháp tồi.
     *  Giá trị 0.15 = 15% xác suất đột biến cho mỗi bài tập. */
    private val mutationRate = 0.15

    /** SỐ BÀI TẬP TỐI ĐA TRONG MỘT LỊCH:
     *  Mỗi cá thể (lịch tập) có từ 3 đến 5 bài tập. */
    private val minExercises = 3
    private val maxExercises = 5

    /* ─────────────────────────────────────────────────────────────────
     *  HÀM CHÍNH: CHẠY THUẬT TOÁN DI TRUYỀN
     * ─────────────────────────────────────────────────────────────────
     *  Đây là hàm điều phối chính, gọi các bước theo thứ tự:
     *  1. Khởi tạo quần thể ban đầu
     *  2. Lặp qua các thế hệ:
     *     a. Đánh giá độ thích nghi
     *     b. Chọn lọc
     *     c. Lai ghép
     *     d. Đột biến
     *  3. Trả về cá thể tốt nhất (lịch tập tối ưu)
     * ───────────────────────────────────────────────────────────────── */
    fun run(): WorkoutPlan {
        // BƯỚC 1: KHỞI TẠO QUẦN THỂ BAN ĐẦU (Initialization)
        // Tạo ra populationSize cá thể ngẫu nhiên.
        // Mỗi cá thể là một danh sách bài tập ngẫu nhiên từ kho bài tập.
        var population = List(populationSize) { createRandomIndividual() }

        // BƯỚC 2: LẶP QUA CÁC THẾ HỆ (Generations Loop)
        // Mỗi thế hệ, quần thể được cải tiến dần dần.
        repeat(generations) {
            // BƯỚC 2a: ĐÁNH GIÁ ĐỘ THÍCH NGHỊ (Fitness Evaluation)
            // Tính "điểm fitness" cho mỗi cá thể.
            // Cá thể nào có tổng calo gần targetCalories nhất → điểm cao nhất.
            val scored = population.map { individual ->
                Pair(individual, calculateFitness(individual))
            }

            // BƯỚC 2b: CHỌN LỌC (Selection)
            // Sắp xếp theo điểm fitness giảm dần, chọn ra các cá thể tốt nhất.
            // Chỉ giữ lại top 50% (những cá thể "mạnh mẽ" nhất).
            val parents = scored
                .sortedByDescending { it.second }
                .take(populationSize / 2)
                .map { it.first }

            // BƯỚC 2c: LAI GHÉP (Crossover) + BƯỚC 2d: ĐỘT BIẾN (Mutation)
            // Từ các cha mẹ đã chọn, tạo ra thế hệ con mới.
            // Mỗi cặp cha mẹ kết hợp gen (bài tập) để tạo con.
            // Sau đó, một số bài tập trong con bị đột biến ngẫu nhiên.
            val children = mutableListOf<List<Exercise>>()
            while (children.size < populationSize) {
                // Chọn ngẫu nhiên 2 cha mẹ từ tập hợp parents
                val parent1 = parents.random()
                val parent2 = parents.random()

                // LAI GHÉP (Crossover): Kết hợp gen của 2 cha mẹ
                val child = crossover(parent1, parent2)

                // ĐỘT BIẾN (Mutation): Thay đổi ngẫu nhiên một gen
                val mutatedChild = mutate(child)

                children.add(mutatedChild)
            }

            // Thế hệ con trở thành quần thể mới cho thế hệ tiếp theo
            population = children
        }

        // BƯỚC 3: CHỌN CÁ THỂ TỐT NHẤT (Best Individual)
        // Sau tất cả các thế hệ, chọn cá thể có điểm fitness cao nhất.
        // Đây chính là lịch tập tối ưu mà thuật toán tìm được.
        val best = population.maxByOrNull { calculateFitness(it) }
            ?: exercisePool.shuffled().take(minExercises)

        return WorkoutPlan(
            exercises = best,
            totalCalories = best.sumOf { it.calories }
        )
    }

    /* ─────────────────────────────────────────────────────────────────
     *  BƯỚC 1: KHỞI TẠO CÁ THỂ NGẪU NHIÊN (Random Initialization)
     * ─────────────────────────────────────────────────────────────────
     *  Tạo một lịch tập ngẫu nhiên bằng cách:
     *  1. Chọn ngẫu nhiên số lượng bài tập (từ minExercises đến maxExercises)
     *  2. Lấy ngẫu nhiên các bài tập từ kho bài tập
     *  ───────────────────────────────────────────────────────────────── */
    private fun createRandomIndividual(): List<Exercise> {
        val count = Random.nextInt(minExercises, maxExercises + 1)
        return exercisePool.shuffled().take(count)
    }

    /* ─────────────────────────────────────────────────────────────────
     *  BƯỚC 2a: HÀM ĐÁNH GIÁ ĐỘ THÍCH NGHỊ (Fitness Function)
     * ─────────────────────────────────────────────────────────────────
     *  Đây là hàm quan trọng nhất trong thuật toán di truyền!
     *  Nó đánh giá "tốt" hay "xấu" của mỗi cá thể (lịch tập).
     *
     *  Công thức: fitness = 1.0 / (1.0 + |targetCalories - totalCalories|)
     *
     *  Giải thích:
     *  - |targetCalories - totalCalories| = khoảng cách giữa calo thực tế
     *    và calo mục tiêu. Càng nhỏ càng tốt!
     *  - Chia 1.0 / (1.0 + khoảng cách) để chuyển thành điểm số.
     *  - Khoảng cách = 0 → fitness = 1.0 (hoàn hảo!)
     *  - Khoảng cách lớn → fitness gần 0 (tệ!)
     *
     *  Ví dụ: targetCalories = 500, tổng calo = 480
     *  → khoảng cách = |500 - 480| = 20
     *  → fitness = 1.0 / (1.0 + 20) = 0.0476
     * ───────────────────────────────────────────────────────────────── */
    private fun calculateFitness(individual: List<Exercise>): Double {
        val totalCalories = individual.sumOf { it.calories }
        val distance = kotlin.math.abs(targetCalories - totalCalories)
        return 1.0 / (1.0 + distance)
    }

    /* ─────────────────────────────────────────────────────────────────
     *  BƯỚC 2c: LAI GHÉP (Crossover)
     * ─────────────────────────────────────────────────────────────────
     *  Lai ghép là quá trình kết hợp gen (bài tập) từ hai cha mẹ
     *  để tạo ra cá thể con mới. Con sẽ thừa hưởng đặc điểm tốt
     *  từ cả cha lẫn mẹ.
     *
     *  Phương pháp: Single-Point Crossover (Lai ghép tại một điểm)
     *  1. Chọn ngẫu nhiên một điểm cắt trong danh sách bài tập
     *  2. Lấy bài tập từ đầu đến điểm cắt của parent1
     *  3. Lấy bài tập từ điểm cắt đến cuối của parent2
     *  4. Ghép lại, loại bỏ bài tập trùng lặp
     *
     *  Ví dụ:
     *  Parent1: [Chạy bộ, Hít đất, Squat]
     *  Parent2: [Nhảy dây, Gập bụng, Plank]
     *  Điểm cắt = 1
     *  → Con: [Chạy bộ] + [Gập bụng, Plank] = [Chạy bộ, Gập bụng, Plank]
     * ───────────────────────────────────────────────────────────────── */
    private fun crossover(parent1: List<Exercise>, parent2: List<Exercise>): List<Exercise> {
        val cutPoint = Random.nextInt(
            1,
            minOf(parent1.size, parent2.size)
        )
        val childGenes = parent1.take(cutPoint) + parent2.drop(cutPoint)
        // Loại bỏ bài tập trùng lặp (giữ thứ tự xuất hiện đầu tiên)
        return childGenes.distinctBy { it.id }
    }

    /* ─────────────────────────────────────────────────────────────────
     *  BƯỚC 2d: ĐỘT BIẾN (Mutation)
     * ─────────────────────────────────────────────────────────────────
     *  Đột biến là quá trình thay đổi ngẫu nhiên một gen (bài tập)
     *  trong cá thể. Mục đích:
     *  1. Duy trì sự đa dạng gen trong quần thể
     *  2. Tránh thuật toán bị "kẹt" ở giải pháp cục bộ (local optimum)
     *  3. Có thể tìm ra bài tập tốt hơn mà lai ghép không tạo được
     *
     *  Cách thực hiện:
     *  - Duyệt qua từng bài tập trong lịch
     *  - Với xác suất mutationRate (15%), thay thế bài tập đó bằng
     *    một bài tập ngẫu nhiên từ kho bài tập
     *  - Nếu lịch sau đột biến quá ngắn (< minExercises), thêm bài tập mới
     *
     *  Ví dụ: Lịch [Chạy bộ, Hít đất, Squat], đột biến bài tập thứ 2
     *  → [Chạy bộ, Nhảy dây, Squat]  (Hít đất bị thay bằng Nhảy dây)
     * ───────────────────────────────────────────────────────────────── */
    private fun mutate(individual: List<Exercise>): List<Exercise> {
        val mutated = individual.map { exercise ->
            // Với xác suất mutationRate, thay thế bài tập bằng bài tập mới
            if (Random.nextDouble() < mutationRate) {
                exercisePool.random()
            } else {
                exercise
            }
        }.distinctBy { it.id } // Loại bỏ trùng lặp sau đột biến

        // Đảm bảo lịch tập có đủ số bài tập tối thiểu
        return if (mutated.size < minExercises) {
            val missing = minExercises - mutated.size
            val extras = exercisePool
                .filter { it.id !in mutated.map { e -> e.id } }
                .shuffled()
                .take(missing)
            mutated + extras
        } else {
            mutated.take(maxExercises) // Giới hạn số bài tập tối đa
        }
    }
}
