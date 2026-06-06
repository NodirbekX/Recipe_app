package com.example.snaprecipe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Friendly empty state. The subtitle differs depending on whether the model failed to
 * identify any food ([detectedFood] == null) vs. found a food but TheMealDB had no
 * matching recipes.
 */
@Composable
fun NotFoundScreen(
    detectedFood: String?,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtitle = if (detectedFood == null) {
        "We couldn't identify any food in this photo. Try a clearer shot of a single dish or ingredient."
    } else {
        "No recipes found for “${detectedFood.replaceFirstChar { it.uppercase() }}”. " +
            "Try a different photo or another ingredient."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🤷‍♂️", fontSize = 88.sp)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "No Recipes Found",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        Button(
            onClick = onTryAgain,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Try Again", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}
