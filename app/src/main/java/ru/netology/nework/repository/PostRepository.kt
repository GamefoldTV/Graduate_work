package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.auth.AuthState
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun upload(upload: MediaUpload): Media


    suspend fun userAuthentication(login : String, pass : String) : AuthState
    suspend fun userRegistration(login : String, pass : String, name : String) : AuthState
    suspend fun userGetById(id : Long) : AuthState
  }

