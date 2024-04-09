package com.example.chattingclone.Data

data class UserData(
    var userId: String? = "",
    var name: String? = "",
    var phone: String? = "",
    var imageUrl: String? = ""
) {
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "phone" to phone,
        "imageUrl" to imageUrl
    )
}

data class ChatData(
    val chatId: String? = "",
    val user1: ChatUser = ChatUser(),
    val user2: ChatUser = ChatUser()
)

data class ChatUser(
    val userId: String? = "",
    val name: String? = "",
    val imageUrl: String? = "",
    val number: String? = ""
)

data class Message(
    val sendBy: String? = "",
    val message: String? = "",
    val timeStamp: String? = ""
)

data class Status(
    val user: ChatUser = ChatUser(),
    val imageUrl: String? = "",
    val timeStamp: Long? = null
)

