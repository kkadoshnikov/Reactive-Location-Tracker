package com.gmail.kadoshnikovkirill.fake.tracks.generator

import com.gmail.kadoshnikovkirill.fake.tracks.generator.core.UserCoordinatesGenerator
import com.gmail.kadoshnikovkirill.fake.tracks.generator.core.UserCoordinatesGeneratorFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.BodyInserters.fromPublisher
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.SynchronousSink
import java.time.Duration

@Service
@ConditionalOnProperty("stream.userCount")
class StreamTracksGenerator(
        @Value("\${stream.userCount}")
        private val userCount: Int,
        userCoordinatesGeneratorFactory: UserCoordinatesGeneratorFactory
) {

    private val webClient: WebClient = WebClient.create("http://localhost:8080/tracks")
    private val fakeUsers = userCoordinatesGeneratorFactory.createList(userCount)

    init {
        trackInitialState()
    }

    private fun trackInitialState() {
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

    // fixedRate = Long.MAX_VALUE isn't a good solution, but it's OK for mock.
    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    fun startTracksStream() {
        fakeUsers.forEach {
            webClient.post()
                    .uri("/stream")
                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                    .accept(MediaType.APPLICATION_STREAM_JSON)
                    .body(fromPublisher(generateCoordinatesFlux(it), UserCoordinatesDto::class.java))
                    .exchange()
                    .map(ClientResponse::statusCode)
                    .log()
                    .subscribe()
        }
    }

    private fun generateCoordinatesFlux(fakeUser: UserCoordinatesGenerator): Flux<UserCoordinatesDto> {
        return Flux.generate { sync: SynchronousSink<UserCoordinatesDto> ->
            sync.next(fakeUser.getCoordinates())
        }.delayElements(Duration.ofMillis(100))
    }
}