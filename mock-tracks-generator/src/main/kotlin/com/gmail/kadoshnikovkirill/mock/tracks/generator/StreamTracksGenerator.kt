package com.gmail.kadoshnikovkirill.mock.tracks.generator

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
import java.time.LocalDateTime.now
import kotlin.random.Random.Default.nextDouble

@Service
@ConditionalOnProperty("stream.userCount")
class StreamTracksGenerator(@Value("\${stream.userCount}") private val userCount: Long) {
    private val webClient: WebClient = WebClient.create("http://localhost:8080/tracks")
    private val minLat = 54.900820
    private val maxLat = 55.0005
    private val minLon = 73.283246
    private val maxLon = 73.490083
    private val maxStep = 0.00002

    private lateinit var userCoordinates: MutableMap<Long, Pair<Double, Double>>

    init {
        userCoordinates = (0 until userCount)
                .associateWith { nextDouble(minLat, maxLat) to nextDouble(minLon, maxLon) }
                .toMutableMap()
        trackInitialState()
    }

    private fun trackInitialState() {
        val now = now()
        userCoordinates
                .map { (userId, coordinates) -> UserCoordinatesDto(
                        userId = userId,
                        timestamp = now,
                        coordinates = coordinates) }
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
        userCoordinates.replaceAll { _, coordinates -> coordinates.nextCoordinates() }
        userCoordinates.keys.forEach { userId ->
            webClient.post()
                    .uri("/stream")
                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                    .accept(MediaType.APPLICATION_STREAM_JSON)
                    .body(fromPublisher(generateCoordinatesFlux(userId), UserCoordinatesDto::class.java))
                    .exchange()
                    .map(ClientResponse::statusCode)
                    .log()
                    .subscribe()
        }
    }

    private fun generateCoordinatesFlux(userId: Long): Flux<UserCoordinatesDto> {
        return Flux.generate { sync: SynchronousSink<UserCoordinatesDto> ->
            val newCoordinates = userCoordinates
                    .computeIfPresent(userId) { _, coordinates -> coordinates.nextCoordinates() }
            newCoordinates?.let { sync.next(UserCoordinatesDto(userId, now(), it)) }
        }.delayElements(Duration.ofMillis(100))
    }

    private fun Pair<Double, Double>.nextCoordinates() = first.nextCoordinate() to second.nextCoordinate()

    private fun Double.nextCoordinate() = this + (Math.random() - 0.5) * maxStep
}