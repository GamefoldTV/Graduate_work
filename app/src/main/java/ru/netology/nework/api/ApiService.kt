package ru.netology.nework.api

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.nework.BuildConfig
import ru.netology.nework.dto.*

private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"

fun okhttp(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .apply {
        interceptors.forEach {
            this.addInterceptor(it)
        }
    }
    .build()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface ApiService {
    //==========POSTS=====================================================================
    @GET("posts")
    suspend fun getAll(): Response<List<PostResponse>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<PostResponse>

    @POST("posts")
    suspend fun save(@Body postRequest: PostRequest): Response<PostResponse>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part file: MultipartBody.Part): Response<MediaResponse>
    //==========Authentication| Users===========================================================
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun userAuthentication(
        @Field("login") login: String,
        @Field("password") password: String
    ): Response<AuthenticationResponse>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun userRegistration(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("name") name: String,
    ): Response<AuthenticationResponse>

    @FormUrlEncoded
    @Multipart
    @POST("users/registration")
    suspend fun userRegistrationWithAvatar(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("name") name: String,
        @Field("file") file: MultipartBody.Part,
    ): Response<AuthenticationResponse>

    @GET("users/{user_id}")
    suspend fun getUserById(@Path("user_id") user_id: Long?): Response<UserResponse>

    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>
    //==========Events===========================================================
    @GET("events")
    suspend fun getEvents(): Response<List<EventResponse>>
    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<EventResponse>
    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<EventResponse>
    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>
    @DELETE("events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") id: Long): Response<EventResponse>
    @POST("events/{id}/participants")
    suspend fun partEventById(@Path("id") id: Long): Response<EventResponse>
    @DELETE("events/{id}/participants")
    suspend fun nonPartEventById(@Path("id") id: Long): Response<EventResponse>
    @POST("events")
    suspend fun saveEvent(@Body eventRequest: EventRequest): Response<EventResponse>
    //==========Wall=============================================================
    @GET("{author_id}/wall/")
    suspend fun getWallById(@Path("author_id") author_id: Long): Response<List<PostResponse>>
    //==========My Wall=============================================================
    @GET("my/wall")
    suspend fun getMyWall(): Response<List<PostResponse>>
    //==========Jobs=============================================================
    @GET("my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>
    @POST("my/jobs")
    suspend fun saveJob(@Body jobRequest: JobRequest): Response<JobResponse>
    @DELETE("my/jobs/{job_id}")
    suspend fun removeJobById(@Path("job_id") job_id: Long): Response<Unit>
    @GET("{user_id}/jobs")
    suspend fun getJobsByUserId(@Path("user_id") user_id: Long): Response<List<JobResponse>>

}