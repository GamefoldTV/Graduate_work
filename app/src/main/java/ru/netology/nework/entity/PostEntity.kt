package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.dao.Converters
import ru.netology.nework.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob : String? = null,
    val content: String,
    val published: String,
    @Embedded
    var coords: CoordEmbeddable?,
    val link: String? = null,
    @TypeConverters(Converters::class)
    val likeOwnerIds:  List<Long>?,
    @TypeConverters(Converters::class)
    val mentionIds:  List<Long>?,
    @TypeConverters(Converters::class)
    val mentionList : List<String>?,
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    @Embedded
    var attachment: AttachmentEmbeddable?,
    val ownedByMe: Boolean = false,
) {

    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        published,
        coords?.toDto(),
        link,
        likeOwnerIds,
        mentionIds,
        mentionList,
        mentionedMe,
        likedByMe,
        attachment?.toDto(),
        ownedByMe,
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.published,
                CoordEmbeddable.fromDto(dto.coords),
                dto.link,
                dto.likeOwnerIds,
                dto.mentionIds,
                dto.mentionList,
                dto.mentionedMe,
                dto.likedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.ownedByMe,
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)