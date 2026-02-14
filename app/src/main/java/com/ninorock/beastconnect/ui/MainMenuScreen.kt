package com.ninorock.beastconnect.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(
    onClassicClick: () -> Unit,
    onCampaignClick: () -> Unit,
    onDailyChallengeClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Beast Connect", fontSize = 32.sp, modifier = Modifier.padding(bottom = 48.dp))
        
        Button(
            onClick = onClassicClick,
            modifier = Modifier.width(200.dp).padding(8.dp)
        ) {
            Text("Classic Mode")
        }
        
        Button(
            onClick = onCampaignClick,
            modifier = Modifier.width(200.dp).padding(8.dp)
        ) {
            Text("Campaign Mode")
        }

        Button(
            onClick = onDailyChallengeClick,
            modifier = Modifier.width(200.dp).padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFA500))
        ) {
            Text("Daily Challenge")
        }
    }
}
