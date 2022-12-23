package ru.netology.nework.dto

data class AuthenticationRequest(
    val id: Long = 0, val token: String? = null
)


