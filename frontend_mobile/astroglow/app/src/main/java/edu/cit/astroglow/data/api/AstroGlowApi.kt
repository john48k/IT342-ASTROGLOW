package edu.cit.astroglow.data.api

import edu.cit.astroglow.data.model.LoginRequest
import edu.cit.astroglow.data.model.LoginResponse
import edu.cit.astroglow.data.model.User
import edu.cit.astroglow.data.model.UserEntity
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

    @POST("api/user/postUser")
    suspend fun postUser(@Body user: UserEntity): Response<UserEntity>

    @GET("/api/user/getAllUser")
    suspend fun getAllUsers(): Response<List<UserEntity>>

    @GET("/api/user/getUserByEmail/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<UserEntity>

    @GET("api/music/search/exact/title")
    suspend fun searchByExactTitle(@Query("title") title: String): Response<List<Music>>

    @POST("api/music/postMusic")
    suspend fun uploadMusic(@Body music: MusicUploadRequest): Response<Music>
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

data class MusicUploadRequest(
    val title: String,
    val artist: String,
    val genre: String,
    val time: Int,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val playlists: List<Long> = emptyList(),
    val offlineLibraries: List<Long> = emptyList(),
    val favorites: List<Long> = emptyList()
) 