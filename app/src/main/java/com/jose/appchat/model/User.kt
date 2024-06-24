package com.jose.appchat.model

data class User(
    var uid: String = "",
    var name: String = "",
    var bio: String = "",
    var profilePicturePath: String? = null,
    val isAdmin: Boolean = false
)
