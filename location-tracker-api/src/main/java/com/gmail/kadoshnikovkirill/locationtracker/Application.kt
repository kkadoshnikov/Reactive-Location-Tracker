package com.gmail.kadoshnikovkirill.locationtracker

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import reactor.tools.agent.ReactorDebugAgent

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    ReactorDebugAgent.init()
    SpringApplication.run(Application::class.java, *args)
}