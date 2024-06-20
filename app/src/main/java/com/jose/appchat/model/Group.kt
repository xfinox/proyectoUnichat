package com.jose.appchat.model


data class Group(
    val id: String = "",
    val name: String = "",
    val admin: String = "",
    val members: MutableList<String> = mutableListOf(),
    val imageUrl: String = ""
)
