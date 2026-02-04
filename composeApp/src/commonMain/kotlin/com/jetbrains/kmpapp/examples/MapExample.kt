package com.jetbrains.kmpapp.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.components.MapView

@Composable
fun MapExample() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Map Example",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Default location: San Francisco
            MapView(
                modifier = Modifier.fillMaxSize().testTag("map_view"),
                latitude = 37.7749,
                longitude = -122.4194,
                zoom = 15f
            )
        }
    }
}
