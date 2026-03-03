package com.ninorock.beastconnect.game

import java.util.Locale

object LanguageManager {
    enum class Language {
        ENGLISH, VIETNAMESE
    }

    var currentLanguage = if (Locale.getDefault().language == "vi") Language.VIETNAMESE else Language.ENGLISH

    fun getString(key: String): String {
        return when (currentLanguage) {
            Language.VIETNAMESE -> vietnameseStrings[key] ?: key
            Language.ENGLISH -> englishStrings[key] ?: key
        }
    }

    private val englishStrings = mapOf(
        "classic_mode" to "CLASSIC MODE",
        "campaign" to "CAMPAIGN",
        "daily_quest" to "DAILY QUEST",
        "relax_and_match" to "Relax and Match",
        "unlock_alpha_beasts" to "Unlock Alpha Beasts",
        "new_puzzle_daily" to "New Puzzle Daily",
        "customize_tiles" to "CUSTOMIZE TILES",
        "beasts" to "BEASTS",
        "food" to "FOOD",
        "gems" to "GEMS",
        "selected" to "SELECTED",
        "watch_ad" to "WATCH AD",
        "level" to "LEVEL",
        "score" to "SCORE",
        "time" to "TIME",
        "shuffle" to "SHUFFLE",
        "hint" to "HINT",
        "resume" to "RESUME",
        "pause" to "PAUSE",
        "quit" to "QUIT",
        "tiles" to "TILES",
        "ready" to "READY?",
        "paused" to "PAUSED",
        "game_over" to "GAME OVER",
        "no_more_moves" to "NO MORE MOVES!",
        "retry" to "RETRY",
        "victory" to "VICTORY!",
        "play_again" to "PLAY AGAIN",
        "level_clear" to "LEVEL %d CLEAR!",
        "continue" to "CONTINUE",
        "awesome" to "AWESOME",
        "new_beast_unlocked" to "NEW BEAST UNLOCKED!",
        "get_ready" to "GET READY!",
        "solve_without_shuffles" to "SOLVE IT WITHOUT SHUFFLES!",
        "daily_challenge" to "DAILY CHALLENGE",
        "extra_time_title" to "Out of time!",
        "extra_shuffles_title" to "Out of shuffles!",
        "extra_hints_title" to "Out of hints!",
        "unlock_food_title" to "Unlock Food Tiles",
        "unlock_gem_title" to "Unlock Gem Tiles",
        "extra_time_desc" to "Watch an ad to get 5 more minutes?",
        "extra_shuffles_desc" to "Watch an ad to get 5 more shuffles?",
        "extra_hints_desc" to "Watch an ad to get 1 more hint?",
        "unlock_food_desc" to "Watch an ad to unlock Food Tiles forever?",
        "unlock_gem_desc" to "Watch an ad to unlock Gem Tiles forever?",
        "watch_ad_button" to "WATCH AD",
        "skip" to "Skip",
        "quit_confirm_title" to "QUIT GAME?",
        "quit_confirm_desc" to "Are you sure you want to quit this game? Your progress will be lost.",
        "yes" to "YES",
        "no" to "NO"
    )

    private val vietnameseStrings = mapOf(
        "classic_mode" to "CHẾ ĐỘ CỔ ĐIỂN",
        "campaign" to "CHIẾN DỊCH",
        "daily_quest" to "THỬ THÁCH NGÀY",
        "relax_and_match" to "Thư giãn và Kết nối",
        "unlock_alpha_beasts" to "Mở khóa Alpha Beasts",
        "new_puzzle_daily" to "Câu đố mới mỗi ngày",
        "customize_tiles" to "TÙY CHỈNH Ô",
        "beasts" to "LINH THÚ",
        "food" to "THỨC ĂN",
        "gems" to "ĐÁ QUÝ",
        "selected" to "ĐANG CHỌN",
        "watch_ad" to "XEM QC",
        "level" to "CẤP ĐỘ",
        "score" to "ĐIỂM SỐ",
        "time" to "THỜI GIAN",
        "shuffle" to "XÁO TRỘN",
        "hint" to "GỢI Ý",
        "resume" to "TIẾP TỤC",
        "pause" to "TẠM DỪNG",
        "quit" to "THOÁT",
        "tiles" to "HÌNH ẢNH",
        "ready" to "SẴN SÀNG?",
        "paused" to "TẠM DỪNG",
        "game_over" to "TRÒ CHƠI KẾT THÚC",
        "no_more_moves" to "HẾT NƯỚC ĐI!",
        "retry" to "THỬ LẠI",
        "victory" to "CHIẾN THẮNG!",
        "play_again" to "CHƠI LẠI",
        "level_clear" to "HOÀN THÀNH CẤP %d!",
        "continue" to "TIẾP TỤC",
        "awesome" to "TUYỆT VỜI",
        "new_beast_unlocked" to "LINH THÚ MỚI ĐÃ MỞ!",
        "get_ready" to "CHUẨN BỊ!",
        "solve_without_shuffles" to "GIẢI ĐỐ KHÔNG CẦN XÁO TRỘN!",
        "daily_challenge" to "THỬ THÁCH NGÀY",
        "extra_time_title" to "Hết thời gian!",
        "extra_shuffles_title" to "Hết lượt trộn!",
        "extra_hints_title" to "Hết lượt gợi ý!",
        "unlock_food_title" to "Mở khóa Food Tiles",
        "unlock_gem_title" to "Mở khóa Gem Tiles",
        "extra_time_desc" to "Xem quảng cáo để nhận thêm 5 phút chơi tiếp?",
        "extra_shuffles_desc" to "Xem quảng cáo để nhận thêm 5 lần trộn?",
        "extra_hints_desc" to "Xem quảng cáo để nhận thêm 1 lượt gợi ý?",
        "unlock_food_desc" to "Xem quảng cáo để mở khóa bộ hình Food Tiles vĩnh viễn?",
        "unlock_gem_desc" to "Xem quảng cáo để mở khóa bộ hình Gem Tiles vĩnh viễn?",
        "watch_ad_button" to "XEM QUẢNG CÁO",
        "skip" to "Bỏ qua",
        "quit_confirm_title" to "THOÁT GAME?",
        "quit_confirm_desc" to "Bạn có chắc chắn muốn thoát? Mọi tiến trình sẽ bị mất.",
        "yes" to "CÓ",
        "no" to "KHÔNG"
    )
}
