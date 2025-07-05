package com.gsr.flashlight

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gsr.flashlight.ui.theme.FlashlightTheme
import com.gsr.flashlight.ui.theme.darkPurple
import com.gsr.flashlight.ui.theme.darkYellow
import com.gsr.flashlight.ui.theme.lightPurple
import com.gsr.flashlight.ui.theme.lightYellow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashlightTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun rememberTorchState(): MutableState<Boolean> {
    val context = LocalContext.current
    val cameraManager = remember {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    val torchOn = rememberSaveable { mutableStateOf(false) }
    DisposableEffect(cameraManager) {
        val cb = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(id: String, enabled: Boolean) {
                torchOn.value = enabled
            }
            override fun onTorchModeUnavailable(id: String) {
                torchOn.value = false      // e.g. another app is using the camera
            }
        }
        cameraManager.registerTorchCallback(cb, Handler(Looper.getMainLooper()))
        onDispose { cameraManager.unregisterTorchCallback(cb) }
    }
    return torchOn
}

fun toggleFlashlight(context: Context, enable: Boolean) {
    try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        cameraManager.setTorchMode(cameraId, enable)
    } catch (e: Exception) {
        Log.e("Flashlight", "Error toggling flashlight: ${e.message}")
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val turnOn = rememberTorchState()
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor by animateColorAsState(
        if (turnOn.value) lightYellow else lightPurple,
        tween(300)
    )
    val switchColor by animateColorAsState(
        if (turnOn.value) darkYellow else darkPurple,
        tween(300)
    )
    LaunchedEffect(turnOn) {
        toggleFlashlight(context, turnOn.value)
    }
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .padding(horizontal = 16.dp, vertical = 60.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(
                            bounded = false,
                            color = Color.White,
                            radius = 300.dp
                        ),
                        onClick = {
                            toggleFlashlight(context, !turnOn.value)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radii = listOf(250f, 350f)
                    for (radius in radii) {
                        drawCircle(
                            color = switchColor,
                            radius = radius,
                            center = Offset(centerX, centerY),
                            alpha = 0.3f,
                            style = Fill
                        )
                    }
                    drawCircle(
                        color = switchColor,
                        radius = 175f,
                        center = Offset(centerX, centerY),
                        alpha = 1f,
                        style = Fill
                    )
                }
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = if (turnOn.value) "Turn flashlight off" else "Turn flashlight on",
                    tint = Color.White,
                    modifier = Modifier.size(70.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    FlashlightTheme {
        MainScreen()
    }
}