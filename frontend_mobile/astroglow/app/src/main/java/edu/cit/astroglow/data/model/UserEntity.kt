package edu.cit.astroglow.data.model

import com.google.gson.annotations.SerializedName

data class UserEntity(
    @SerializedName("userId")
    val id: Long? = null,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("userPassword")
    val userPassword: String,
    @SerializedName("userEmail")
    val userEmail: String,
    @SerializedName("authentication")
    val authentication: AuthenticationEntity? = null,
    @SerializedName("playlists")
    val playlists: List<Any> = emptyList(),
    @SerializedName("offlineLibraries")
    val offlineLibraries: List<Any> = emptyList(),
    @SerializedName("favorites")
    val favorites: List<Any> = emptyList()
)

data class AuthenticationEntity(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("type")
    var type: String = ""
) 