package ru.netology.nework.dto

data class JobResponse(
    val id : Long,
    val name : String,
    val position : String,
    val start : String,
    val finish : String?,
    val link : String?
)