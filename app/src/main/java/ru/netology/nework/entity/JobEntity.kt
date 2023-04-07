package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val userId: Long,
    val ownedByMe: Boolean = false,
    val name: String,
    val position: String,
    val start : String,
    val finish : String? = null,
    val link: String? = null,

) {
    fun toDto() = Job(
        userId,
        ownedByMe,
        id,
        name,
        position,
        start,
        finish,
        link,
    )

    companion object {
        fun fromDto(dto: Job) =
            JobEntity(
                dto.id,
                dto.userId,
                dto.ownedByMe,
                dto.name,
                dto.position,
                dto.start,
                dto.finish,
                dto.link
            )
    }
}

fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
fun List<Job>.toEntity(): List<JobEntity> = map(JobEntity::fromDto)