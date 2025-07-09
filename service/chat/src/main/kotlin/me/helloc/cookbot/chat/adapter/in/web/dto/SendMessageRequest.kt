package me.helloc.cookbot.chat.adapter.`in`.web.dto

data class SendMessageRequest(
    val message: String,
    val userId: String
)