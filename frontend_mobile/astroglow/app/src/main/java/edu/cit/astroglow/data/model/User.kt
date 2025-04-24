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
    val authentication: AuthenticationEntity? = null,
    @SerializedName("music")
    val music: List<Any>? = null,
    @SerializedName("playlists")
    val playlists: List<Any>? = null,
    @SerializedName("offlineLibraries")
    val offlineLibraries: List<Any>? = null,
    @SerializedName("favorites")
    val favorites: List<Any>? = null
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