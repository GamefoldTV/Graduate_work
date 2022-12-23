package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Users

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val avatar: String? = null,
) {

    fun toDto() = Users(
        id,
        name,
        avatar,
    )

    companion object {
        fun fromDto(dto: Users) =
            UserEntity(
                dto.id,
                dto.name,
                dto.avatar
            )
    }
}

fun List<UserEntity>.toDto(): List<Users> = map(UserEntity::toDto)
fun List<Users>.toEntity(): List<UserEntity> = map(UserEntity::fromDto)