package me.helloc.cookbot.chat.adapter.`in`.web

import me.helloc.cookbot.chat.application.port.`in`.AiChatUseCase
import me.helloc.cookbot.chat.application.port.`in`.SendMessageCommand
import me.helloc.cookbot.chat.application.port.out.ChatResponse
import java.time.LocalDateTime

class FakeAiChatUseCase : AiChatUseCase {
    private val responses = mutableListOf<String>()
    private var currentIndex = 0

    fun addResponse(response: String) {
        responses.add(response)
    }

    override fun sendMessage(command: SendMessageCommand): ChatResponse {
        val message = if (responses.isEmpty()) {
            "AI가 생성한 레시피 응답"
        } else {
            val response = responses[currentIndex % responses.size]
            currentIndex++
            response
        }

        return ChatResponse(
            message = message,
            timestamp = LocalDateTime.now()
        )
    }
}
