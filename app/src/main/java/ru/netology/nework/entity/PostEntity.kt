package ru.netology.nework.entity

import androidx.room.*
import ru.netology.nework.dao.Converters
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import java.util.*
import java.util.stream.Collectors

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    @Embedded
    var coords: CoordEmbeddable?,
    val link: String? = null,
    @TypeConverters(Converters::class)
    val likeOwnerIds:  List<Long>?,
    @TypeConverters(Converters::class)
    val mentionIds:  List<Long>?,
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
        content,
        published,
        coords?.toDto(),
        link,
        likeOwnerIds,
        mentionIds,
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
                dto.content,
                dto.published,
                CoordEmbeddable.fromDto(dto.coords),
                dto.link,
                dto.likeOwnerIds,
                dto.mentionIds,
                dto.mentionedMe,
                dto.likedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.ownedByMe,
            )
    }
}

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

data class CoordEmbeddable(
    val latitude : String,
    val longitude : String,
) {
    fun toDto() =
        Coordinates(latitude, longitude)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordEmbeddable(it.lat, it.long)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)

