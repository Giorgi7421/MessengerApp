package ge.gpavliashvili.messenger.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.gpavliashvili.messenger.data.model.Conversation
import ge.gpavliashvili.messenger.data.repository.AuthRepository
import ge.gpavliashvili.messenger.data.repository.MessageRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val messageRepository = MessageRepository()

    private val _conversations = MutableLiveData<List<Conversation>>()
    val conversations: LiveData<List<Conversation>> = _conversations

    private val _filteredConversations = MutableLiveData<List<Conversation>>()
    val filteredConversations: LiveData<List<Conversation>> = _filteredConversations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var allConversations = listOf<Conversation>()
    private var searchQuery = ""

    init {
        loadConversations()
    }

    private fun loadConversations() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                _isLoading.value = true
                val result = messageRepository.getConversations(currentUser.uid)
                result.onSuccess { conversationList ->
                    allConversations = conversationList
                    _conversations.value = conversationList
                    applyFilter()
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load conversations"
                }
                _isLoading.value = false
            }
        }
    }

    fun searchConversations(query: String) {
        searchQuery = query
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (searchQuery.isEmpty()) {
            allConversations
        } else {
            allConversations.filter { conversation ->
                conversation.otherUser?.nickname?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        _filteredConversations.value = filtered
    }

    fun refreshConversations() {
        loadConversations()
    }

    fun logout() {
        authRepository.logout()
    }
} 