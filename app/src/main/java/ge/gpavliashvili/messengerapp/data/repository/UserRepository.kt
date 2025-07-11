package ge.gpavliashvili.messengerapp.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ge.gpavliashvili.messengerapp.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun getAllUsers(currentUserId: String): Result<List<User>> {
        return try {
            val snapshot = database.child("users").get().await()
            val users = mutableListOf<User>()
            snapshot.children.forEach { child ->
                val user = child.getValue(User::class.java)
                if (user != null && user.uid != currentUserId) {
                    users.add(user)
                }
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String, currentUserId: String): Result<List<User>> {
        return try {
            val snapshot = database.child("users").get().await()
            val users = mutableListOf<User>()
            snapshot.children.forEach { child ->
                val user = child.getValue(User::class.java)
                if (user != null && user.uid != currentUserId && 
                    user.nickname.contains(query, ignoreCase = true)) {
                    users.add(user)
                }
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val snapshot = database.child("users").child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
            user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                snapshot.children.forEach { child ->
                    val user = child.getValue(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }
                trySend(users)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.child("users").addValueEventListener(listener)
        awaitClose { database.child("users").removeEventListener(listener) }
    }
} 