package com.ninorock.beastconnect.game

import android.app.Activity
import android.util.Log
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.LeaderboardsClient

class LeaderboardManager(private val activity: Activity) {

    private val gamesSignInClient: GamesSignInClient = PlayGames.getGamesSignInClient(activity)
    private val leaderboardsClient: LeaderboardsClient = PlayGames.getLeaderboardsClient(activity)

    companion object {
        const val LEADERBOARD_CLASSIC = "CgkInpmcqfQUEAIQAA"
        const val LEADERBOARD_CAMPAIGN = "CgkInpmcqfQUEAIQAQ"
        private const val TAG = "LeaderboardManager"
    }

    fun signIn(onComplete: (Boolean) -> Unit = {}) {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask ->
            val isAuthenticated = isAuthenticatedTask.isSuccessful && isAuthenticatedTask.result.isAuthenticated
            if (isAuthenticated) {
                Log.d(TAG, "User already authenticated with Play Games")
                onComplete(true)
            } else {
                Log.d(TAG, "User not authenticated, attempting silent sign-in/prompt")
                gamesSignInClient.signIn().addOnCompleteListener { signInTask ->
                    val success = signInTask.isSuccessful && signInTask.result.isAuthenticated
                    Log.d(TAG, "Sign-in success: $success")
                    onComplete(success)
                }
            }
        }
    }

    fun submitScore(mode: GameMode, score: Int) {
        val leaderboardId = when (mode) {
            GameMode.CLASSIC -> LEADERBOARD_CLASSIC
            GameMode.CAMPAIGN -> LEADERBOARD_CAMPAIGN
            else -> return
        }
        
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.isAuthenticated) {
                leaderboardsClient.submitScore(leaderboardId, score.toLong())
                Log.d(TAG, "Score submitted: $score to $leaderboardId")
            } else {
                Log.d(TAG, "Cannot submit score: User not authenticated")
            }
        }
    }

    fun showLeaderboard(leaderboardId: String? = null) {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.isAuthenticated) {
                val intentTask = if (leaderboardId != null) {
                    leaderboardsClient.getLeaderboardIntent(leaderboardId)
                } else {
                    leaderboardsClient.allLeaderboardsIntent
                }
                
                intentTask.addOnSuccessListener { intent ->
                    activity.startActivityForResult(intent, 9004)
                }
            } else {
                signIn { success ->
                    if (success) showLeaderboard(leaderboardId)
                }
            }
        }
    }
}
