package ge.gpavliashvili.messenger.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.gpavliashvili.messenger.data.model.User
import ge.gpavliashvili.messenger.data.repository.AuthRepository
import ge.gpavliashvili.messenger.data.repository.StorageRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val storageRepository = StorageRepository()

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _imageUploadResult = MutableLiveData<Result<String>>()
    val imageUploadResult: LiveData<Result<String>> = _imageUploadResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val firebaseUser = authRepository.getCurrentUser()
        if (firebaseUser != null) {
            viewModelScope.launch {
                _isLoading.value = true
                val result = authRepository.getUserData(firebaseUser.uid)
                result.onSuccess { user ->
                    _currentUser.value = user
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load user data"
                }
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(nickname: String, profession: String) {
        val currentUserValue = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updatedUser = currentUserValue.copy(
                nickname = nickname.trim(),
                profession = profession.trim()
            )
            val result = authRepository.updateUserProfile(updatedUser)
            _updateResult.value = result
            if (result.isSuccess) {
                _currentUser.value = updatedUser
            }
            _isLoading.value = false
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        val currentUserValue = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = storageRepository.uploadProfileImage(currentUserValue.uid, imageUri)
            result.onSuccess { downloadUrl ->
                val updatedUser = currentUserValue.copy(profileImageUrl = downloadUrl)
                val updateResult = authRepository.updateUserProfile(updatedUser)
                if (updateResult.isSuccess) {
                    _currentUser.value = updatedUser
                    _imageUploadResult.value = Result.success(downloadUrl)
                } else {
                    _imageUploadResult.value = Result.failure(Exception("Failed to update profile"))
                }
            }.onFailure { exception ->
                _imageUploadResult.value = Result.failure(exception)
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun isValidNickname(nickname: String): Boolean {
        return nickname.trim().length >= 3
    }

    fun isValidProfession(profession: String): Boolean {
        return profession.trim().isNotEmpty()
    }
} 