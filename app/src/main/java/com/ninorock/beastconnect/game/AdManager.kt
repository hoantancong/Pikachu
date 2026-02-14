package com.ninorock.beastconnect.game

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917" // Test Rewarded Ad ID

    init {
        MobileAds.initialize(context)
        loadRewardedAd()
    }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit) {
        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                onRewardEarned()
                loadRewardedAd() // Load next ad
            }
        } ?: run {
            // If ad not loaded, you might want to try loading it again or inform the user
            loadRewardedAd()
        }
    }

    fun isAdLoaded(): Boolean = rewardedAd != null
}
