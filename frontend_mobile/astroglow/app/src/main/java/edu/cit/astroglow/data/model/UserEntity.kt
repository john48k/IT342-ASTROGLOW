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
    val playlists: List<Any>? = null,
    @SerializedName("offlineLibraries")
    val offlineLibraries: List<Any>? = null,
    @SerializedName("favorites")
    val favorites: List<Any>? = null
)

data class AuthenticationEntity(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("type")
    var type: String = "",
    @SerializedName("biometricEnabled")
    var biometricEnabled: Boolean = false
) 