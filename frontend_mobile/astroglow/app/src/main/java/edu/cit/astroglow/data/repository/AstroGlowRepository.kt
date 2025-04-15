package edu.cit.astroglow.data.repository

import edu.cit.astroglow.data.api.AstroGlowApi
import edu.cit.astroglow.data.api.Music
import edu.cit.astroglow.data.api.Playlist
import edu.cit.astroglow.data.model.LoginRequest
import edu.cit.astroglow.data.model.LoginResponse
import edu.cit.astroglow.data.model.User
import retrofit2.Response

class AstroGlowRepository(private val api: AstroGlowApi) {
    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        return api.login(loginRequest)
    }

    suspend fun register(user: User): Response<User> {
        return api.register(user)
    }

    suspend fun getUser(id: Long): Response<User> {
        return api.getUser(id)
    }

    suspend fun getAllMusic(): Response<List<Music>> {
        return api.getAllMusic()
    }

    suspend fun getFavorites(): Response<List<Music>> {
        return api.getFavorites()
    }

    suspend fun addToFavorites(musicId: Long): Response<Unit> {
        return api.addToFavorites(musicId)
    }

    suspend fun removeFromFavorites(musicId: Long): Response<Unit> {
        return api.removeFromFavorites(musicId)
    }

    suspend fun getPlaylists(): Response<List<Playlist>> {
        return api.getPlaylists()
    }

    suspend fun createPlaylist(playlist: Playlist): Response<Playlist> {
        return api.createPlaylist(playlist)
    }
} 