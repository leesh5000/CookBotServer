package me.helloc.cookbot.chat.adapter.`in`.web

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.helloc.cookbot.chat.adapter.`in`.web.dto.SendMessageRequest

class ChatControllerTest : StringSpec({

    "should return chat response when message is sent" {
        val fakeChatUseCase = FakeAiChatUseCase()
        fakeChatUseCase.addResponse("테스트 응답")

        val aiChatController = AiChatController(fakeChatUseCase)

        val request = SendMessageRequest(
            message = "테스트 메시지",
            userId = "user123"
        )

        val response = aiChatController.sendMessage(request)

        response.message shouldBe "테스트 응답"
        response.timestamp shouldNotBe null
    }

    "should return default response when no specific response is set" {
        val fakeChatUseCase = FakeAiChatUseCase()
        val aiChatController = AiChatController(fakeChatUseCase)

        val request = SendMessageRequest(
            message = "아무 메시지",
            userId = "user456"
        )

        val response = aiChatController.sendMessage(request)

        response.message shouldBe "AI가 생성한 레시피 응답"
        response.timestamp shouldNotBe null
    }

    "should handle multiple requests" {
        val fakeChatUseCase = FakeAiChatUseCase()
        fakeChatUseCase.addResponse("첫 번째 레시피")
        fakeChatUseCase.addResponse("두 번째 레시피")

        val aiChatController = AiChatController(fakeChatUseCase)

        val firstRequest = SendMessageRequest(
            message = "첫 번째 요청",
            userId = "user123"
        )

        val secondRequest = SendMessageRequest(
            message = "두 번째 요청",
            userId = "user123"
        )

        val firstResponse = aiChatController.sendMessage(firstRequest)
        val secondResponse = aiChatController.sendMessage(secondRequest)

        firstResponse.message shouldBe "첫 번째 레시피"
        secondResponse.message shouldBe "두 번째 레시피"
    }
})
