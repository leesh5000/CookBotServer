package me.helloc.cookbot.chat.adapter.out.openai

import me.helloc.cookbot.chat.application.port.out.AiChatPort
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.stereotype.Component

@Component
class OpenAIAiChatAdapter(
    private val chatModel: OpenAiChatModel
) : AiChatPort {

    override fun generateResponse(message: String): String {
        return chatModel.call(message)
    }
}
