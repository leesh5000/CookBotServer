package me.helloc.cookbot.chat.application.port.`in`

import me.helloc.cookbot.chat.application.port.out.ChatResponse

interface AiChatUseCase {
    fun sendMessage(command: SendMessageCommand): ChatResponse
}

data class SendMessageCommand(
    val message: String,
    val userId: String
)
