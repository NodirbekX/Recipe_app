package com.example.snaprecipe.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.snaprecipe.ui.theme.Amber200
import com.example.snaprecipe.ui.theme.Amber400
import com.example.snaprecipe.ui.theme.Amber600
import com.example.snaprecipe.ui.theme.Cream
import com.example.snaprecipe.util.ImageUtils
import java.io.File

/**
 * Landing screen: branded header, a big circular camera shutter, and two entry
 * points (camera / gallery). Owns the camera-permission flow and the
 * ActivityResult launchers, decoding the chosen image to a Bitmap before handing
 * it up via [onImageSelected].
 */
@Composable
fun HomeScreen(
    onImageSelected: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // The FileProvider URI the camera writes the full-resolution capture to.
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                ImageUtils.decodeBitmapFromUri(context, uri)?.let(onImageSelected)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { ImageUtils.decodeBitmapFromUri(context, it)?.let(onImageSelected) }
    }

    fun launchCamera() {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(imagesDir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        cameraImageUri = uri
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera() else showSettingsDialog = true
    }

    fun onTakePhoto() {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) launchCamera() else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun onPickGallery() {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // Warm orange/amber gradient background.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Amber600, Amber400, Amber200, Cream)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ---- Title ----
            HeaderBadge(icon = Icons.Filled.Restaurant)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "SnapRecipe",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Snap a photo. Discover recipes.",
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // ---- Big circular shutter button ----
            Surface(
                onClick = ::onTakePhoto,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .size(140.dp)
                    .shadow(16.dp, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Inner amber ring for the shutter look.
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .background(Amber600.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Take a photo",
                            tint = Amber600,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // ---- Action buttons ----
            Button(
                onClick = ::onTakePhoto,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Amber600
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("📷  Take Photo", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = ::onPickGallery,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("🖼️  Choose from Gallery", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }

    // Shown when the camera permission has been permanently denied.
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Camera permission needed") },
            text = {
                Text(
                    "SnapRecipe needs camera access to take photos of your food. " +
                        "You can enable it in Settings, or pick a photo from your gallery instead."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }) { Text("Open Settings") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/** Small fork+spoon-style header badge above the title. */
@Composable
private fun HeaderBadge(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.22f),
        modifier = Modifier.size(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
