/**
 * RegisterScreen.kt
 * 注册页面 - 田园治愈风格
 */

package com.keling.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import com.keling.app.R
import com.keling.app.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val registerState by viewModel.registerState.collectAsState()

    // 表单状态
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // 表单验证
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // 注册成功回调
    LaunchedEffect(registerState) {
        if (registerState is UiState.Success) {
            onRegisterSuccess()
        }
    }

    // 验证函数
    fun validateForm(): Boolean {
        var isValid = true

        // 验证用户名
        if (name.isBlank()) {
            nameError = "请输入用户名"
            isValid = false
        } else if (name.length < 2) {
            nameError = "用户名至少2个字符"
            isValid = false
        } else {
            nameError = null
        }

        // 验证邮箱
        if (email.isBlank()) {
            emailError = "请输入邮箱"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "邮箱格式不正确"
            isValid = false
        } else {
            emailError = null
        }

        // 验证密码
        if (password.isBlank()) {
            passwordError = "请输入密码"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "密码至少6位"
            isValid = false
        } else {
            passwordError = null
        }

        // 验证确认密码
        if (confirmPassword.isBlank()) {
            confirmPasswordError = "请确认密码"
            isValid = false
        } else if (confirmPassword != password) {
            confirmPasswordError = "两次密码不一致"
            isValid = false
        } else {
            confirmPasswordError = null
        }

        return isValid
    }

    // 背景动画
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CreamWhite,
                        MintGreenLight,
                        SkyBlueLight
                    )
                )
            )
    ) {
        // 装饰元素
        Box(
            modifier = Modifier
                .offset(x = (300 - offset * 0.015f).dp, y = 80.dp)
                .size(180.dp)
                .background(
                    MintGreen.copy(alpha = 0.1f),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .offset(x = (-80 + offset * 0.02f).dp, y = 500.dp)
                .size(160.dp)
                .background(
                    LavenderPurple.copy(alpha = 0.1f),
                    CircleShape
                )
        )

        // 主内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "课灵Logo",
                modifier = Modifier
                    .size(100.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 标题
            Text(
                text = "创建账号",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = EarthBrown
            )

            Text(
                text = "开启你的知识培育之旅",
                fontSize = 14.sp,
                color = EarthBrown.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 注册卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 错误提示
                    AnimatedVisibility(
                        visible = registerState is UiState.Error,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        val errorMessage = (registerState as? UiState.Error)?.message ?: ""
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = RoseRedLight
                            )
                        ) {
                            Text(
                                text = errorMessage,
                                color = RoseRedDark,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 用户名输入
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text("用户名") },
                        singleLine = true,
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it, color = RoseRed) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            focusedLabelColor = MintGreen,
                            cursorColor = MintGreen,
                            errorBorderColor = RoseRed
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 邮箱输入
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = { Text("邮箱") },
                        singleLine = true,
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = RoseRed) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            focusedLabelColor = MintGreen,
                            cursorColor = MintGreen,
                            errorBorderColor = RoseRed
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 密码输入
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = { Text("密码") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(
                                    text = if (passwordVisible) "隐藏" else "显示",
                                    color = MintGreen,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it, color = RoseRed) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            focusedLabelColor = MintGreen,
                            cursorColor = MintGreen,
                            errorBorderColor = RoseRed
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 确认密码输入
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = null
                        },
                        label = { Text("确认密码") },
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Text(
                                    text = if (confirmPasswordVisible) "隐藏" else "显示",
                                    color = MintGreen,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        isError = confirmPasswordError != null,
                        supportingText = confirmPasswordError?.let { { Text(it, color = RoseRed) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            focusedLabelColor = MintGreen,
                            cursorColor = MintGreen,
                            errorBorderColor = RoseRed
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (validateForm()) {
                                    viewModel.register(name.trim(), email.trim(), password)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 注册按钮
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (validateForm()) {
                                viewModel.register(name.trim(), email.trim(), password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintGreen
                        ),
                        enabled = registerState !is UiState.Loading
                    ) {
                        if (registerState is UiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "注 册",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 登录入口
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已有账号？",
                    color = EarthBrown.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "立即登录",
                        color = MintGreenDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}