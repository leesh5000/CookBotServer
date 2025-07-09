package me.helloc.cookbot.chat.adapter.`in`.web.dto

import java.time.LocalDateTime

data class ChatResponse(
    val message: String,
    val timestamp: LocalDateTime
)