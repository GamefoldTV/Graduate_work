package ru.netology.nework.dto

data class Users(
    val id: Long,
    val name: String,
    val avatar: String? = null,
)

