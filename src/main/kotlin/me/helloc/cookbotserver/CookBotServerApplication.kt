package me.helloc.cookbotserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CookBotServerApplication

fun main(args: Array<String>) {
    runApplication<CookBotServerApplication>(*args)
}
