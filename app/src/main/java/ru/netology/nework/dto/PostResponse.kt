package ru.netology.nework.dto

data class PostResponse(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    val coords : Coordinates? = null,
    val link : String? = null,
    val likeOwnerIds : List<Long>? = emptyList(),
    val mentionIds : List<Long>? = emptyList(),
    val mentionedMe : Boolean = false,
    val likedByMe : Boolean = false,
    val attachment : Attachment? = null,
    val ownedByMe : Boolean = false,
    val users : UserList?,
)

data class UserList (
    val user: ArrayList<UserPreview>
)

data class UserPreview(
    val name : String,
    val avatar : String? = null,
)