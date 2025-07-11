package ge.gpavliashvili.messengerapp.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ge.gpavliashvili.messengerapp.data.model.Conversation
import ge.gpavliashvili.messengerapp.data.model.Message
import ge.gpavliashvili.messengerapp.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MessageRepository {
    private val database = FirebaseDatabase.getInstance().reference
    private val userRepository = UserRepository()

    suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            val messageId = database.child("messages").push().key ?: ""
            val messageWithId = message.copy(id = messageId)
            
            database.child("messages").child(messageId).setValue(messageWithId).await()

            updateConversation(message.senderId, message.receiverId, message.text, message.timestamp)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateConversation(senderId: String, receiverId: String, lastMessage: String, timestamp: Long) {
        try {
            val conversationId = getConversationId(senderId, receiverId)
            val conversation = mapOf(
                "id" to conversationId,
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to lastMessage,
                "lastMessageTimestamp" to timestamp,
                "lastMessageSenderId" to senderId
            )
            
            database.child("conversations").child(conversationId).setValue(conversation).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun getConversationId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    fun getMessagesForConversation(userId1: String, userId2: String): Flow<List<Message>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                snapshot.children.forEach { child ->
                    val message = child.getValue(Message::class.java)
                    if (message != null && 
                        ((message.senderId == userId1 && message.receiverId == userId2) ||
                         (message.senderId == userId2 && message.receiverId == userId1))) {
                        messages.add(message)
                    }
                }
                messages.sortBy { it.timestamp }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.child("messages").addValueEventListener(listener)
        awaitClose { database.child("messages").removeEventListener(listener) }
    }

    suspend fun getConversations(currentUserId: String): Result<List<Conversation>> {
        return try {
            val snapshot = database.child("conversations").get().await()
            val conversations = mutableListOf<Conversation>()
            
            snapshot.children.forEach { child ->
                val conversationData = child.value as? Map<String, Any>
                if (conversationData != null) {
                    val participants = conversationData["participants"] as? List<String> ?: emptyList()
                    if (participants.contains(currentUserId)) {
                        val otherUserId = participants.find { it != currentUserId }
                        if (otherUserId != null) {
                            val otherUserResult = userRepository.getUserById(otherUserId)
                            otherUserResult.getOrNull()?.let { otherUser ->
                                val conversation = Conversation(
                                    id = conversationData["id"] as? String ?: "",
                                    participants = participants,
                                    lastMessage = conversationData["lastMessage"] as? String ?: "",
                                    lastMessageTimestamp = conversationData["lastMessageTimestamp"] as? Long ?: 0L,
                                    lastMessageSenderId = conversationData["lastMessageSenderId"] as? String ?: "",
                                    otherUser = otherUser
                                )
                                conversations.add(conversation)
                            }
                        }
                    }
                }
            }
            
            conversations.sortByDescending { it.lastMessageTimestamp }
            Result.success(conversations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getConversationsFlow(currentUserId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = mutableListOf<Conversation>()
                
                snapshot.children.forEach { child ->
                    val conversationData = child.value as? Map<String, Any>
                    if (conversationData != null) {
                        val participants = conversationData["participants"] as? List<String> ?: emptyList()
                        if (participants.contains(currentUserId)) {
                            val otherUserId = participants.find { it != currentUserId }
                            if (otherUserId != null) {
                                val conversation = Conversation(
                                    id = conversationData["id"] as? String ?: "",
                                    participants = participants,
                                    lastMessage = conversationData["lastMessage"] as? String ?: "",
                                    lastMessageTimestamp = conversationData["lastMessageTimestamp"] as? Long ?: 0L,
                                    lastMessageSenderId = conversationData["lastMessageSenderId"] as? String ?: ""
                                )
                                conversations.add(conversation)
                            }
                        }
                    }
                }
                
                conversations.sortByDescending { it.lastMessageTimestamp }
                trySend(conversations)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.child("conversations").addValueEventListener(listener)
        awaitClose { database.child("conversations").removeEventListener(listener) }
    }
} 