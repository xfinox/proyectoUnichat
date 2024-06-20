package com.jose.appchat.model

import java.io.Serializable

data class UserGroups(
    var id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = ""
) : Serializable
