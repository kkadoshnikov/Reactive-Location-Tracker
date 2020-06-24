package com.gmail.kadoshnikovkirill.fake.tracks.generator

import com.gmail.kadoshnikovkirill.fake.tracks.generator.core.UserCoordinatesGeneratorFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient

@Service
@ConditionalOnProperty("serial.userCount")
class SerialTracksGenerator(
        @Value("\${serial.userCount}")
        private val userCount: Int,
        userCoordinatesGeneratorFactory: UserCoordinatesGeneratorFactory
) {

    private val webClient: WebClient = WebClient.create("localhost:8080/tracks")
    private val fakeUsers = userCoordinatesGeneratorFactory.createList(userCount)

    init {
        sendTracks() // Send first time with empty cache
    }

    // We need initial delay because first time takes more time (due to empty cache)
    @Scheduled(initialDelay = 5000, fixedRate = 250)
    fun generateTracks() {
        sendTracks()
    }

    private fun sendTracks() {
        fakeUsers.map { it.getCoordinates() }
                .forEach {
                    webClient.post()
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(it))
                            .exchange()
                            .map(ClientResponse::statusCode)
                            .log()
                            .subscribe()
        }
    }
}