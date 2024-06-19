package com.jose.appchat.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var name: String = "",
    var bio: String = "",
    var profilePicturePath: String? = null,
    var uid: String = "",
    var email: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "bio" to bio,
            "profilePicturePath" to profilePicturePath,
            "uid" to uid,
            "email" to email
        )
    }
}
