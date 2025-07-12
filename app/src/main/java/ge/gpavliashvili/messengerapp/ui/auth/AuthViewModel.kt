package ge.gpavliashvili.messenger.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import ge.gpavliashvili.messenger.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<Result<FirebaseUser>>()
    val loginResult: LiveData<Result<FirebaseUser>> = _loginResult

    private val _registerResult = MutableLiveData<Result<FirebaseUser>>()
    val registerResult: LiveData<Result<FirebaseUser>> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.login(email, password)
            _loginResult.value = result
            _isLoading.value = false
        }
    }

    fun loginWithNickname(nickname: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.loginWithNickname(nickname, password)
            _loginResult.value = result
            _isLoading.value = false
        }
    }

    fun register(nickname: String, email: String, password: String, profession: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.register(nickname, email, password, profession)
            _registerResult.value = result
            _isLoading.value = false
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidNickname(nickname: String): Boolean {
        return nickname.trim().length >= 3
    }

    fun isValidProfession(profession: String): Boolean {
        return profession.trim().isNotEmpty()
    }
} 