package com.jose.appchat.model

data class Group(
    var id: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val createdBy: String = "",
    var members: List<String> = listOf()
)
