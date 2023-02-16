package ru.netology.nework.repository

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.*
import ru.netology.nework.auth.AuthState
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.*
import ru.netology.nework.entity.*
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postdao: PostDao,
    private val eventdao: EventDao,
    private val userdao: UserDao,
    private val apiService: ApiService,
) : PostRepository {
    override val posts = postdao.getPosts()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)
    override val events = eventdao.getEvents()
        .map(List<EventEntity>::toDto)
        .flowOn(Dispatchers.Default)
    override val users = userdao.getUsers()
        .map(List<UserEntity>::toDto)
        .flowOn(Dispatchers.Default)


    override suspend fun getPosts() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val post = bodyResponse.map {
                Post(
                    it.id,
                    it.authorId,
                    it.author,
                    it.authorAvatar,
                    it.authorJob,
                    it.content,
                    it.published,
                    it.coords,
                    it.link,
                    it.likeOwnerIds,
                    it.mentionIds,
                    it.mentionedMe,
                    it.likedByMe,
                    it.attachment,
                    it.ownedByMe
                )
            }
            val users = bodyResponse.map {
                it.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            }
            postdao.insert(post.toEntity())
            users.map {
                if (it != null) {
                    userdao.insert(it.toEntity())
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaRequest): MediaResponse {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val postRequest = PostRequest(
                post.id, post.content,
                post.coords,
                post.link,
                post.attachment, post.mentionIds
            )
            val response = apiService.save(postRequest)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val postResponse = Post(
                bodyResponse.id,
                bodyResponse.authorId,
                bodyResponse.author,
                bodyResponse.authorAvatar,
                bodyResponse.authorJob,
                bodyResponse.content,
                bodyResponse.published,
                bodyResponse.coords,
                bodyResponse.link,
                bodyResponse.likeOwnerIds,
                bodyResponse.mentionIds,
                bodyResponse.mentionedMe,
                bodyResponse.likedByMe,
                bodyResponse.attachment,
                bodyResponse.ownedByMe
            )
            val users =
                bodyResponse.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }

            postdao.insert(PostEntity.fromDto(postResponse))
            users?.map {
                userdao.insert(UserEntity.fromDto(it))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaRequest) {
        try {
            val media = upload(upload)
            // TODO: add support for other types
            val postWithAttachment =
                post.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postdao.removeById(id)
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = apiService.getById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val post =
                response.body()?.let {
                    Post(
                        it.id,
                        it.authorId,
                        it.author,
                        it.authorAvatar,
                        it.authorJob,
                        it.content,
                        it.published,
                        it.coords,
                        it.link,
                        it.likeOwnerIds,
                        it.mentionIds,
                        it.mentionedMe,
                        it.likedByMe,
                        it.attachment,
                        it.ownedByMe
                        )
                } ?: throw ApiError(response.code(), response.message())

            if (post.likedByMe) {
                val dislikedPost = post.copy(likedByMe = false)//, likes = post.likes - 1)
                postdao.insert(PostEntity.fromDto(dislikedPost))
                val response = apiService.dislikeById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
            } else {
                val likedPost = post.copy(likedByMe = true)//, likes = post.likes + 1)
                postdao.insert(PostEntity.fromDto(likedPost))
                val response = apiService.likeById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun userAuthentication(login: String, password: String): AuthState {
        try {
            val authResponse = apiService.userAuthentication(login, password)

            if (!authResponse.isSuccessful) {
                throw ApiError(authResponse.code(), authResponse.message())
            }

            val userById = apiService.getUserById(authResponse.body()?.id)

            if (!userById.isSuccessful) {
                throw ApiError(userById.code(), userById.message())
            }

            val id = authResponse.body()?.id ?: 0
            val token = authResponse.body()?.token
            val name = userById.body()?.name

            return AuthState(id, token, name)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun userRegistration(
        login: String,
        password: String,
        name: String
    ): AuthState {
        try {
            val authResponse = apiService.userRegistration(login, password, name)

            if (!authResponse.isSuccessful) {
                throw ApiError(authResponse.code(), authResponse.message())
            }

            val id = authResponse.body()?.id ?: 0
            val token = authResponse.body()?.token

            return AuthState(id, token, name)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun userRegistrationWithAvatar(
        login: String,
        password: String,
        name: String,
        avatar: MediaRequest
    ): AuthState {
        try {
            // val media = upload(avatar)
            val media = MultipartBody.Part.createFormData(
                "file", avatar.file.name, avatar.file.asRequestBody()
            )

            val authResponse = apiService.userRegistrationWithAvatar(login, password, name, media)

            if (!authResponse.isSuccessful) {
                throw ApiError(authResponse.code(), authResponse.message())
            }

            val id = authResponse.body()?.id ?: 0
            val token = authResponse.body()?.token

            return AuthState(id, token, name)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getUsersByIds(ids: List<Long>): List<Users> {
        TODO("Not yet implemented")
    }

    override suspend fun getEvents() {
        try {
            val response = apiService.getEvents()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val event = bodyResponse.map {
                Event(
                    it.id,
                    it.authorId,
                    it.author,
                    it.authorAvatar,
                    it.authorJob,
                    it.content,
                    it.datetime,
                    it.published,
                    it.coords,
                    it.type,
                    it.likeOwnerIds,
                    it.likedByMe,
                    it.speakerIds,
                    it.participantsIds,
                    it.participatedByMe,
                    it.attachment,
                    it.link,
                    it.ownedByMe
                )
            }
            val users = bodyResponse.map {
                it.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            }
            eventdao.insert(event.toEntity())
            users.map {
                if (it != null) {
                    userdao.insert(it.toEntity())
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEvent(event: Event) {
        try {
            val eventRequest = EventRequest(
                event.id,
                event.content,
                event.datetime,
                event.coords,
                event.type,
                event.attachment,
                event.link,
                event.speakerIds
            )
            val response = apiService.saveEvent(eventRequest)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val eventResponse = Event(
                bodyResponse.id,
                bodyResponse.authorId,
                bodyResponse.author,
                bodyResponse.authorAvatar,
                bodyResponse.authorJob,
                bodyResponse.content,
                bodyResponse.datetime,
                bodyResponse.published,
                bodyResponse.coords,
                bodyResponse.type,
                bodyResponse.likeOwnerIds,
                bodyResponse.likedByMe,
                bodyResponse.speakerIds,
                bodyResponse.participantsIds,
                bodyResponse.participatedByMe,
                bodyResponse.attachment,
                bodyResponse.link,
                bodyResponse.ownedByMe
            )
            val users =
                bodyResponse.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            eventdao.insert(EventEntity.fromDto(eventResponse))
            users?.map {
                userdao.insert(UserEntity.fromDto(it))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEventWithAttachment(event: Event, upload: MediaRequest) {
        try {
            val media = upload(upload)
            val eventWithAttachment =
                event.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            saveEvent(eventWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeEventById(id: Long, likedByMe: Boolean) {
        try {
            if (likedByMe) {
                val response = apiService.dislikeEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.let {
                        Event(
                            it.id,
                            it.authorId,
                            it.author,
                            it.authorAvatar,
                            it.authorJob,
                            it.content,
                            it.datetime,
                            it.published,
                            it.coords,
                            it.type,
                            it.likeOwnerIds,
                            it.likedByMe,
                            it.speakerIds,
                            it.participantsIds,
                            it.participatedByMe,
                            it.attachment,
                            it.link,
                            it.ownedByMe
                        )
                    } ?: throw ApiError(response.code(), response.message())
                eventdao.insert(EventEntity.fromDto(event))
            } else {
                val response = apiService.likeEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.let {
                        Event(
                            it.id,
                            it.authorId,
                            it.author,
                            it.authorAvatar,
                            it.authorJob,
                            it.content,
                            it.datetime,
                            it.published,
                            it.coords,
                            it.type,
                            it.likeOwnerIds,
                            it.likedByMe,
                            it.speakerIds,
                            it.participantsIds,
                            it.participatedByMe,
                            it.attachment,
                            it.link,
                            it.ownedByMe
                        )
                    } ?: throw ApiError(response.code(), response.message())
                eventdao.insert(EventEntity.fromDto(event))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeEventById(id: Long) {
        try {
            eventdao.removeById(id)
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun partEventById(id: Long, participatedByMe: Boolean) {
        try {
            if (participatedByMe) {
                val response = apiService.nonPartEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.let {
                        Event(
                            it.id,
                            it.authorId,
                            it.author,
                            it.authorAvatar,
                            it.authorJob,
                            it.content,
                            it.datetime,
                            it.published,
                            it.coords,
                            it.type,
                            it.likeOwnerIds,
                            it.likedByMe,
                            it.speakerIds,
                            it.participantsIds,
                            it.participatedByMe,
                            it.attachment,
                            it.link,
                            it.ownedByMe
                        )
                    } ?: throw ApiError(response.code(), response.message())
                eventdao.insert(EventEntity.fromDto(event))
            } else {
                val response = apiService.partEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.let {
                        Event(
                            it.id,
                            it.authorId,
                            it.author,
                            it.authorAvatar,
                            it.authorJob,
                            it.content,
                            it.datetime,
                            it.published,
                            it.coords,
                            it.type,
                            it.likeOwnerIds,
                            it.likedByMe,
                            it.speakerIds,
                            it.participantsIds,
                            it.participatedByMe,
                            it.attachment,
                            it.link,
                            it.ownedByMe
                        )
                    } ?: throw ApiError(response.code(), response.message())
                eventdao.insert(EventEntity.fromDto(event))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getUsers() {
        try {
            val response = apiService.getUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val users = bodyResponse.map {
                Users(
                    it.id,
                    it.name,
                    it.avatar
                )
            }
            userdao.insert(users.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}
