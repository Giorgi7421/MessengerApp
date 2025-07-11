package ge.gpavliashvili.messengerapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import ge.gpavliashvili.messengerapp.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithNickname(nickname: String, password: String): Result<FirebaseUser> {
        return try {
            val emailResult = getEmailByNickname(nickname)
            if (emailResult.isFailure) {
                return Result.failure(Exception("User not found"))
            }
            
            val email = emailResult.getOrNull()!!
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getEmailByNickname(nickname: String): Result<String> {
        return try {
            val snapshot = database.child("users")
                .orderByChild("nickname")
                .equalTo(nickname)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.children.firstOrNull()?.let { child ->
                    val user = child.getValue(User::class.java)
                    user?.email?.let { email ->
                        Result.success(email)
                    } ?: Result.failure(Exception("Email not found"))
                } ?: Result.failure(Exception("User not found"))
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(nickname: String, email: String, password: String, profession: String): Result<FirebaseUser> {
        return try {
            val nicknameSnapshot = database.child("users")
                .orderByChild("nickname")
                .equalTo(nickname)
                .get()
                .await()

            if (nicknameSnapshot.exists()) {
                return Result.failure(Exception("Nickname already exists"))
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val user = User(
                    uid = firebaseUser.uid,
                    nickname = nickname,
                    profession = profession,
                    email = email
                )
                database.child("users").child(firebaseUser.uid).setValue(user).await()
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("User creation failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun getUserData(uid: String): Result<User> {
        return try {
            val snapshot = database.child("users").child(uid).get().await()
            val user = snapshot.getValue(User::class.java)
            user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("User data not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            database.child("users").child(user.uid).setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 