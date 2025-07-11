package ge.gpavliashvili.messengerapp.data.model

data class User(
    val uid: String = "",
    val nickname: String = "",
    val profession: String = "",
    val profileImageUrl: String = "",
    val email: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "nickname" to nickname,
            "profession" to profession,
            "profileImageUrl" to profileImageUrl,
            "email" to email
        )
    }
} 