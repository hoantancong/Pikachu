package com.ninorock.beastconnect.ui

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object ClassicGame : Screen("classic_game")
    object CampaignGame : Screen("campaign_game")
    object DailyChallenge : Screen("daily_challenge")
}
