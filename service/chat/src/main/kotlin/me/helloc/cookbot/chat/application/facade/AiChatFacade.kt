package me.helloc.cookbot.chat.application.facade

import me.helloc.cookbot.chat.application.port.`in`.AiChatUseCase
import me.helloc.cookbot.chat.application.port.`in`.SendMessageCommand
import me.helloc.cookbot.chat.application.port.out.AiChatPort
import me.helloc.cookbot.chat.application.port.out.ChatResponse
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AiChatFacade(
    private val aiChatPort: AiChatPort
) : AiChatUseCase {

    override fun sendMessage(command: SendMessageCommand): ChatResponse {
        val aiResponse = aiChatPort.generateResponse(command.message)

        return ChatResponse(
            message = aiResponse,
            timestamp = LocalDateTime.now()
        )
    }
}
