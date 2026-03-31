/**
 * AuthViewModel.kt
 * 认证视图模型 - 管理用户登录注册状态
 */

package com.keling.app.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.keling.app.data.AuthRepository
import com.keling.app.data.AuthState
import com.keling.app.network.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * UI状态
 */
sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

/**
 * 认证视图模型
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    // 认证状态
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 登录状态
    private val _loginState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<AuthResponse>> = _loginState.asStateFlow()

    // 注册状态
    private val _registerState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<AuthResponse>> = _registerState.asStateFlow()

    // 是否已登录
    val isLoggedIn: StateFlow<Boolean>
        get() = MutableStateFlow(_authState.value is AuthState.Authenticated).asStateFlow()

    init {
        // 初始化时立即检查认证状态
        checkAuthState()
    }

    /**
     * 检查认证状态
     * 使用 first() 只获取一次当前状态，避免持续监听
     */
    fun checkAuthState() {
        viewModelScope.launch {
            // 先从 DataStore 获取当前状态
            val state = authRepository.getAuthState().first()
            _authState.value = state

            // 然后开始持续监听状态变化
            authRepository.getAuthState().collect { newState ->
                _authState.value = newState
            }
        }
    }

    /**
     * 用户登录
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading

            val result = authRepository.login(email, password)

            result.fold(
                onSuccess = { response ->
                    if (response.error != null) {
                        _loginState.value = UiState.Error(response.error)
                    } else {
                        _loginState.value = UiState.Success(response)
                    }
                },
                onFailure = { error ->
                    _loginState.value = UiState.Error(error.message ?: "登录失败，请重试")
                }
            )
        }
    }

    /**
     * 用户注册
     */
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading

            val result = authRepository.register(name, email, password)

            result.fold(
                onSuccess = { response ->
                    if (response.error != null) {
                        _registerState.value = UiState.Error(response.error)
                    } else {
                        _registerState.value = UiState.Success(response)
                    }
                },
                onFailure = { error ->
                    _registerState.value = UiState.Error(error.message ?: "注册失败，请重试")
                }
            )
        }
    }

    /**
     * 用户登出
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = UiState.Idle
            _registerState.value = UiState.Idle
        }
    }

    /**
     * 重置登录状态
     */
    fun resetLoginState() {
        _loginState.value = UiState.Idle
    }

    /**
     * 重置注册状态
     */
    fun resetRegisterState() {
        _registerState.value = UiState.Idle
    }
}