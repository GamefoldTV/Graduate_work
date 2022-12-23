package ru.netology.nework.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.auth.AuthState
import ru.netology.nework.auth.LoginFormState
import ru.netology.nework.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AppAuth,
    private val repository: PostRepository
) : ViewModel() {
    val data: LiveData<AuthState> = auth
        .authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = auth.authStateFlow.value.id == 0L


    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    fun loginDataChanged(username: String, password: String) {
        _loginForm.value = LoginFormState(isDataValid = isUserNameValid(username) && isPasswordValid(password))
    }

    private fun isUserNameValid(username: String): Boolean {
        return username.isNotEmpty()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }

    private fun isNameValid(password: String): Boolean {
        return password.isNotEmpty()
    }

    fun userAuthentication(login : String, password : String)  =  viewModelScope.launch {
        try {
            _loginForm.value = LoginFormState(isLoading = true)
            val account = repository.userAuthentication(login, password)
            auth.setAuth(account.id, account.token, account.name)
            _loginForm.value = LoginFormState(isDataValid = true)
        } catch (e: Exception) {
            _loginForm.value = LoginFormState(isError = true, isDataValid = true)
        }
    }

    fun userRegistration(login : String, password : String, name : String)  =  viewModelScope.launch {
        try {
            _loginForm.value = LoginFormState(isLoading = true)
            val account = repository.userRegistration(login, password, name)
            auth.setAuth(account.id, account.token, account.name)
            _loginForm.value = LoginFormState(isDataValid = true)
        } catch (e: Exception) {
            _loginForm.value = LoginFormState(isError = true, isDataValid = true)
        }
    }
}
