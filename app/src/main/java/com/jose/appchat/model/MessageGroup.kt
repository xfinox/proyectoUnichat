package com.jose.appchat.model

/*data class MessageGroup(
    var id: String = "",
    val senderId: String = "",
    var senderName: String = "",
    var senderProfileUrl: String = "", // Nueva URL para la foto de perfil del remitente
    val text: String = "",
    val timestamp: Long = 0



    data class MessageGroup(
    var senderId: String? = null,
    var text: String? = null,
    var timestamp: Long = 0
)


)*/

data class MessageGroup(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0
)


