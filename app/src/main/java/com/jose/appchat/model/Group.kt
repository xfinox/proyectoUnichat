package com.jose.appchat.model

/*
* data class Group(
    var id: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val createdBy: String = "",
    var members: List<String> = listOf()
)
* */


data class Group(
    var groupId: String? = null,
    var name: String? = null,
    var photoUrl: String? = null,
    var members: List<String>? = null,
    var roles: Map<String, String>? = null
)

