package ru.netology.nework.dao

import androidx.room.TypeConverter
import ru.netology.nework.enumeration.AttachmentType

class Converters {
    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name

    @TypeConverter
    fun fromListDto(list: List<Long>?): String? {
        if (list == null) return ""
        return list.toString()
    }

    @TypeConverter
    fun toListDto(data: String?): List<Long>? {
        if (data == "[]") return emptyList<Long>()
        else {
            val substr = data?.substring(1, data.length - 1)
            return substr?.split(", ")?.map { it.toLong() }
        }
    }
}
