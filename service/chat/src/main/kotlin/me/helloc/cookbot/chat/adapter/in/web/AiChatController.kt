package me.helloc.cookbot.chat.adapter.`in`.web

import me.helloc.cookbot.chat.adapter.`in`.web.dto.ChatResponse
import me.helloc.cookbot.chat.adapter.`in`.web.dto.SendMessageRequest
import me.helloc.cookbot.chat.application.port.`in`.AiChatUseCase
import me.helloc.cookbot.chat.application.port.`in`.SendMessageCommand
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class AiChatController(
    private val aiChatUseCase: AiChatUseCase
) {

    @PostMapping("/send")
    fun sendMessage(@RequestBody request: SendMessageRequest): ChatResponse {
        val command = SendMessageCommand(
            message = request.message,
            userId = request.userId
        )

        val response = aiChatUseCase.sendMessage(command)

        return ChatResponse(
            message = response.message,
            timestamp = response.timestamp
        )
    }
}
