package me.helloc.cookbot.chat.application.port.out

import java.time.LocalDateTime

interface AiChatPort {
    fun generateResponse(message: String): String
}

data class ChatResponse(
    val message: String,
    val timestamp: LocalDateTime
)
