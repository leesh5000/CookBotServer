package me.helloc.cookbot.chat.application.facade

import me.helloc.cookbot.chat.application.port.out.AiChatPort

class FakeAiChatPort : AiChatPort {
    private val responses = mutableListOf<String>()
    private var currentIndex = 0

    fun addResponse(response: String) {
        responses.add(response)
    }

    override fun generateResponse(message: String): String {
        if (responses.isEmpty()) {
            return "단백질 60g을 채울 수 있는 닭가슴살 볶음밥 레시피를 추천드립니다."
        }

        val response = responses[currentIndex % responses.size]
        currentIndex++
        return response
    }
}
