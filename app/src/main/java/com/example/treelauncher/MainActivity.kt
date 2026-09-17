package com.example.treelauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch
import kotlin.math.max

data class AppInfo(
    val name: CharSequence,
    val icon: Drawable,
    val packageName: String,
    val className: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val installedApps = getInstalledApps()
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF2F5F8)
                ) {
                    TreeLauncherScreen(apps = installedApps)
                }
            }
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        return resolveInfos.map {
            AppInfo(
                name = it.loadLabel(pm),
                icon = it.loadIcon(pm),
                packageName = it.activityInfo.packageName,
                className = it.activityInfo.name
            )
        }.sortedBy { it.name.toString() }
    }
}

@Composable
fun Surface(modifier: Modifier = Modifier, color: Color, content: @Composable () -> Unit) {
    Box(modifier = modifier.background(color)) {
        content()
    }
}

@Composable
fun TreeLauncherScreen(apps: List<AppInfo>) {
    var offsetY by remember { mutableStateOf(0f) }
    val maxScroll = max(0f, apps.size * 100f - 800f) // Arbitrary estimation
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetY = (offsetY + dragAmount.y).coerceIn(-maxScroll, 0f)
                }
            }
    ) {
        // Draw the main trunk
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val trunkPath = Path().apply {
                moveTo(width / 2, height)
                lineTo(width / 2, 0f) // simple straight trunk for now
            }
            drawPath(
                path = trunkPath,
                color = Color(0xFF5D4037),
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )

            // Draw branches based on apps and scroll offset
            apps.forEachIndexed { index, app ->
                val branchY = height - (index * 120f) + offsetY
                if (branchY in -100f..height + 100f) {
                    val isLeft = index % 2 == 0
                    val branchXEnd = if (isLeft) width / 2 - 150f else width / 2 + 150f
                    val branchPath = Path().apply {
                        moveTo(width / 2, branchY + 40f) // start a bit lower
                        quadraticBezierTo(
                            if (isLeft) width / 2 - 50f else width / 2 + 50f, branchY,
                            branchXEnd, branchY - 20f
                        )
                    }
                    drawPath(
                        path = branchPath,
                        color = Color(0xFF6D4C41),
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Draw Apps (Leaves)
        apps.forEachIndexed { index, app ->
            val isLeft = index % 2 == 0
            val branchY = LocalDensity.current.run { (LocalContext.current.resources.displayMetrics.heightPixels).toDp() } - (index * 120).dp + LocalDensity.current.run { offsetY.toDp() }
            val screenHeight = LocalDensity.current.run { LocalContext.current.resources.displayMetrics.heightPixels.toDp() }

            if (branchY > -50.dp && branchY < screenHeight + 50.dp) {
                val branchXEnd = if (isLeft) LocalDensity.current.run { (LocalContext.current.resources.displayMetrics.widthPixels / 2 - 150).toDp() } else LocalDensity.current.run { (LocalContext.current.resources.displayMetrics.widthPixels / 2 + 150).toDp() }
                
                Box(
                    modifier = Modifier
                        .offset(x = branchXEnd - 24.dp, y = branchY - 44.dp)
                        .size(48.dp)
                        .clickable {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        }
                ) {
                    Image(
                        painter = rememberDrawablePainter(drawable = app.icon),
                        contentDescription = app.name.toString(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // App Name Label
                Text(
                    text = app.name.toString(),
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier.offset(
                        x = if (isLeft) branchXEnd - 60.dp else branchXEnd + 30.dp,
                        y = branchY - 20.dp
                    )
                )
            }
        }
    }
}
