package com.keling.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.keling.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 版本更新对话框
 */
@Composable
fun UpdateDialog(
    versionInfo: VersionInfo,
    forceUpdate: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Dialog(
        onDismissRequest = { if (!forceUpdate && !isDownloading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !forceUpdate && !isDownloading,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DawnWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "发现新版本",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EarthBrown
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 版本号
                Text(
                    text = "v${versionInfo.versionName}",
                    fontSize = 16.sp,
                    color = WarmSunOrange,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 更新日志
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BeigeSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = versionInfo.updateLog,
                            fontSize = 14.sp,
                            color = EarthBrown,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 下载进度或错误信息
                if (isDownloading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = downloadProgress,
                            color = WarmSunOrange,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "正在下载... ${(downloadProgress * 100).toInt()}%",
                            fontSize = 14.sp,
                            color = EarthBrown
                        )
                    }
                } else if (downloadError != null) {
                    Text(
                        text = downloadError ?: "",
                        fontSize = 14.sp,
                        color = StellarOrange,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (forceUpdate) Arrangement.Center else Arrangement.spacedBy(12.dp)
                ) {
                    if (!forceUpdate) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isDownloading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("稍后提醒", color = EarthBrown)
                        }
                    }

                    Button(
                        onClick = {
                            if (versionInfo.updateUrl.startsWith("http")) {
                                // 打开浏览器下载
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.updateUrl))
                                context.startActivity(intent)
                            } else {
                                // 应用内下载（暂不支持，直接打开浏览器）
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"))
                                context.startActivity(intent)
                            }
                            onConfirm()
                        },
                        enabled = !isDownloading,
                        modifier = Modifier.weight(if (forceUpdate) 1f else 1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarmSunOrange
                        )
                    ) {
                        Text(if (isDownloading) "下载中..." else "立即更新")
                    }
                }

                // 文件大小提示
                if (versionInfo.fileSize > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "安装包大小: ${formatFileSize(versionInfo.fileSize)}",
                        fontSize = 12.sp,
                        color = EarthBrown.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024))
    }
}

/**
 * 安装APK
 */
fun installApk(context: Context, file: File) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // Android 7.0+ 使用FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    } else {
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    context.startActivity(intent)
}