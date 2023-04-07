package ru.netology.nework.dto

data class Job(
    val userId : Long,
    val ownedByMe : Boolean = false,
    val id : Long,
    val name : String,
    val position : String,
    val start : String,
    val finish : String? = null,
    val link : String? = null,

)
