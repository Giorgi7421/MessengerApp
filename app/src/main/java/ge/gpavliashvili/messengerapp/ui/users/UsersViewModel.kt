package ge.gpavliashvili.messenger.ui.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.gpavliashvili.messenger.data.model.User
import ge.gpavliashvili.messenger.data.repository.AuthRepository
import ge.gpavliashvili.messenger.data.repository.UserRepository
import ge.gpavliashvili.messenger.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _searchResults = MutableLiveData<List<User>>()
    val searchResults: LiveData<List<User>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _hasSearchResults = MutableLiveData<Boolean>()
    val hasSearchResults: LiveData<Boolean> = _hasSearchResults

    private var searchJob: Job? = null
    private var isSearchMode = false
    private var currentPage = 0
    private val pageSize = 20
    private var hasMoreData = true

    init {
        loadUsers()
    }

    private fun loadUsers() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                _isLoading.value = true
                val result = userRepository.getAllUsers(currentUser.uid)
                result.onSuccess { userList ->
                    _users.value = userList
                    if (!isSearchMode) {
                        _hasSearchResults.value = userList.isNotEmpty()
                    }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load users"
                }
                _isLoading.value = false
            }
        }
    }

    fun searchUsers(query: String) {
        searchJob?.cancel()
        
        if (query.length < Constants.MIN_SEARCH_QUERY_LENGTH) {
            isSearchMode = false
            _searchResults.value = emptyList()
            _hasSearchResults.value = _users.value?.isNotEmpty() ?: false
            return
        }

        isSearchMode = true
        searchJob = viewModelScope.launch {
            delay(Constants.DEBOUNCE_DELAY)
            _isSearching.value = true
            
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                val result = userRepository.searchUsers(query, currentUser.uid)
                result.onSuccess { userList ->
                    _searchResults.value = userList
                    _hasSearchResults.value = userList.isNotEmpty()
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Search failed"
                    _hasSearchResults.value = false
                }
            }
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        isSearchMode = false
        _searchResults.value = emptyList()
        _hasSearchResults.value = _users.value?.isNotEmpty() ?: false
    }

    fun refreshUsers() {
        loadUsers()
    }

    fun getCurrentUsersToDisplay(): List<User> {
        return if (isSearchMode) {
            _searchResults.value ?: emptyList()
        } else {
            _users.value ?: emptyList()
        }
    }
} 