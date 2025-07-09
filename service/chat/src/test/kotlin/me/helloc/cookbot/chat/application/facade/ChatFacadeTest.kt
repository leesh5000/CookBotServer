package me.helloc.cookbot.chat.application.facade

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.helloc.cookbot.chat.application.port.`in`.SendMessageCommand

class ChatFacadeTest : StringSpec({

    "should generate AI response for user message" {
        val fakeChatAIPort = FakeAiChatPort()
        fakeChatAIPort.addResponse("닭가슴살 볶음밥 레시피를 추천드립니다.")

        val chatFacade = AiChatFacade(fakeChatAIPort)

        val command = SendMessageCommand(
            message = "단백질 60g을 채울 수 있는 레시피 알려줘",
            userId = "user123"
        )

        val response = chatFacade.sendMessage(command)

        response.message shouldBe "닭가슴살 볶음밥 레시피를 추천드립니다."
        response.timestamp shouldNotBe null
    }

    "should return default response when no specific response is set" {
        val fakeChatAIPort = FakeAiChatPort()
        val chatFacade = AiChatFacade(fakeChatAIPort)

        val command = SendMessageCommand(
            message = "아무 메시지",
            userId = "user123"
        )

        val response = chatFacade.sendMessage(command)

        response.message shouldBe "단백질 60g을 채울 수 있는 닭가슴살 볶음밥 레시피를 추천드립니다."
        response.timestamp shouldNotBe null
    }

    "should handle multiple messages with different responses" {
        val fakeChatAIPort = FakeAiChatPort()
        fakeChatAIPort.addResponse("첫 번째 응답")
        fakeChatAIPort.addResponse("두 번째 응답")

        val chatFacade = AiChatFacade(fakeChatAIPort)

        val firstCommand = SendMessageCommand(
            message = "첫 번째 메시지",
            userId = "user123"
        )

        val secondCommand = SendMessageCommand(
            message = "두 번째 메시지",
            userId = "user123"
        )

        val firstResponse = chatFacade.sendMessage(firstCommand)
        val secondResponse = chatFacade.sendMessage(secondCommand)

        firstResponse.message shouldBe "첫 번째 응답"
        secondResponse.message shouldBe "두 번째 응답"
    }
})
