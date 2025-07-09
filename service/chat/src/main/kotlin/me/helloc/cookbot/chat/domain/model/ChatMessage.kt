package me.helloc.cookbot.chat.domain.model

import java.time.LocalDateTime

data class ChatMessage(
    val id: String,
    val content: String,
    val role: ChatRole,
    val timestamp: LocalDateTime
)

enum class ChatRole {
    USER, ASSISTANT
}