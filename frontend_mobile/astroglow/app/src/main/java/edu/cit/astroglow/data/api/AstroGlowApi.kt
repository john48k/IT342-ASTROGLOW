package edu.cit.astroglow.data.api

import edu.cit.astroglow.data.model.LoginRequest
import edu.cit.astroglow.data.model.LoginResponse
import edu.cit.astroglow.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface AstroGlowApi {
    @POST("api/user/signup")
    suspend fun register(@Body user: User): Response<User>

    @POST("api/user/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: Long): Response<User>

    @GET("api/music")
    suspend fun getAllMusic(): Response<List<Music>>

    @GET("api/favorites")
    suspend fun getFavorites(): Response<List<Music>>

    @POST("api/favorites/{musicId}")
    suspend fun addToFavorites(@Path("musicId") musicId: Long): Response<Unit>

    @DELETE("api/favorites/{musicId}")
    suspend fun removeFromFavorites(@Path("musicId") musicId: Long): Response<Unit>

    @GET("api/playlists")
    suspend fun getPlaylists(): Response<List<Playlist>>

    @POST("api/playlists")
    suspend fun createPlaylist(@Body playlist: Playlist): Response<Playlist>
}

data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: String,
    val url: String,
    val coverImage: String
)

data class Playlist(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val userId: Long,
    val musicList: List<Music> = emptyList()
) 