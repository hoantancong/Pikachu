package com.ninorock.beastconnect.game

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    
    private val rewardedAdUnitId = "ca-app-pub-7200414901906199/7619720970" // Test Rewarded Ad ID
    private val interstitialAdUnitId = "ca-app-pub-7200414901906199/1297855208" // Test Interstitial Ad ID

    init {
        MobileAds.initialize(context)
        loadRewardedAd()
        loadInterstitialAd()
    }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, rewardedAdUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
            }
        })
    }

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, interstitialAdUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }
        })
    }

    fun showRewardedAd(activity: Activity, onAdDismissed: () -> Unit, onRewardEarned: () -> Unit) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd()
                    onAdDismissed()
                }
            }
            ad.show(activity) { rewardItem ->
                onRewardEarned()
            }
        } ?: run {
            loadRewardedAd()
            onAdDismissed()
        }
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitialAd()
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } ?: run {
            loadInterstitialAd()
            onAdDismissed()
        }
    }

    fun isAdLoaded(): Boolean = rewardedAd != null
    fun isInterstitialAdLoaded(): Boolean = interstitialAd != null
}
