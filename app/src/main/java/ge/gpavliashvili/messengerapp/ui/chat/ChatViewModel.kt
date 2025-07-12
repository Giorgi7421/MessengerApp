package ge.gpavliashvili.messenger.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.gpavliashvili.messenger.data.model.Message
import ge.gpavliashvili.messenger.data.model.User
import ge.gpavliashvili.messenger.data.repository.AuthRepository
import ge.gpavliashvili.messenger.data.repository.MessageRepository
import ge.gpavliashvili.messenger.data.repository.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val messageRepository = MessageRepository()
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _otherUser = MutableLiveData<User>()
    val otherUser: LiveData<User> = _otherUser

    private val _sendMessageResult = MutableLiveData<Result<Unit>>()
    val sendMessageResult: LiveData<Result<Unit>> = _sendMessageResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var currentUser = authRepository.getCurrentUser()
    private var otherUserId: String = ""

    fun initChat(otherUserId: String) {
        this.otherUserId = otherUserId
        loadOtherUser()
        loadMessages()
    }

    private fun loadOtherUser() {
        viewModelScope.launch {
            val result = userRepository.getUserById(otherUserId)
            result.onSuccess { user ->
                _otherUser.value = user
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load user data"
            }
        }
    }

    private fun loadMessages() {
        if (currentUser != null) {
            viewModelScope.launch {
                messageRepository.getMessagesForConversation(currentUser!!.uid, otherUserId)
                    .collect { messageList ->
                        _messages.value = messageList
                    }
            }
        }
    }

    fun sendMessage(text: String) {
        if (currentUser == null || text.trim().isEmpty()) return

        val message = Message(
            senderId = currentUser!!.uid,
            receiverId = otherUserId,
            text = text.trim(),
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val result = messageRepository.sendMessage(message)
            _sendMessageResult.value = result
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to send message"
            }
        }
    }

    fun getCurrentUserId(): String {
        return currentUser?.uid ?: ""
    }
} 