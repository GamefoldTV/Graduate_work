package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.auth.AuthState
import ru.netology.nework.dto.*

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun upload(upload: MediaRequest): MediaResponse
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaRequest)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun userAuthentication(login: String, pass: String): AuthState
    suspend fun userRegistration(login: String, pass: String, name: String): AuthState
    suspend fun userRegistrationWithAvatar(
        login: String,
        pass: String,
        name: String,
        avatar: MediaRequest
    ): AuthState

}

