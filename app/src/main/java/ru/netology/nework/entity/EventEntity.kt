package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.dao.Converters
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.EventType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob : String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded
    var coords: CoordEmbeddable?,
    val type : EventType,
    @TypeConverters(Converters::class)
    val likeOwnerIds:  List<Long>?,
    val likedByMe: Boolean = false,
    @TypeConverters(Converters::class)
    val speakerIds : List<Long>?,
    @TypeConverters(Converters::class)
    val speakerList : List<String>?,
    @TypeConverters(Converters::class)
    val participantsIds : List<Long>?,
    @TypeConverters(Converters::class)
    val participantsList : List<String>?,
    val participatedByMe : Boolean,
    @Embedded
    var attachment: AttachmentEmbeddable?,
    val link: String? = null,
    val ownedByMe: Boolean = false,
) {
    fun toDto() = Event(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        datetime,
        published,
        coords?.toDto(),
        type,
        likeOwnerIds,
        likedByMe,
        speakerIds,
        speakerList,
        participantsIds,
        participantsList,
        participatedByMe,
        attachment?.toDto(),
        link,
        ownedByMe,
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.datetime,
                dto.published,
                CoordEmbeddable.fromDto(dto.coords),
                dto.type,
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.speakerIds,
                dto.speakerList,
                dto.participantsIds,
                dto.participantsList,
                dto.participatedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.link,
                dto.ownedByMe,
            )
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)