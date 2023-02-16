package ru.netology.nework.entity

import ru.netology.nework.dto.Attachment
import ru.netology.nework.enumeration.AttachmentType

data class AttachmentEmbeddable(
    var url: String,
    var attachmentType: AttachmentType,
) {
    fun toDto() = Attachment(url, attachmentType)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}
