package edu.cit.astroglow.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("userId")
    val id: Long? = null,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("userEmail")
    val userEmail: String,
    @SerializedName("userPassword")
    val userPassword: String? = null,
    @SerializedName("profilePicture")
    val profilePicture: String? = null,
    @SerializedName("authentication")
    val authentication: String? = null,
    @SerializedName("music")
    val music: List<Any> = emptyList(),
    @SerializedName("playlists")
    val playlists: List<Any> = emptyList(),
    @SerializedName("offlineLibraries")
    val offlineLibraries: List<Any> = emptyList(),
    @SerializedName("favorites")
    val favorites: List<Any> = emptyList()
)

data class LoginRequest(
    @SerializedName("userEmail")
    val userEmail: String,
    @SerializedName("userPassword")
    val userPassword: String,
    @SerializedName("rememberMe")
    val rememberMe: Boolean = true
)

// The login endpoint returns the User object directly
typealias LoginResponse = User 