package com.gmail.kadoshnikovkirill.mock.tracks.generator

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import kotlin.random.Random

@Service
@ConditionalOnProperty("serial.userCount")
class SerialTracksGenerator(@Value("\${serial.userCount}") private val userCount: Long) {
    private val webClient: WebClient = WebClient.create("localhost:8080/tracks")
    private val minLat = 54.900820
    private val maxLat = 55.0005
    private val minLon = 73.283246
    private val maxLon = 73.490083
    private val maxStep = 0.00002

    private lateinit var userCoordinates: MutableMap<Long, Pair<Double, Double>>

    init {
        userCoordinates = (0 until userCount)
                .associateWith { Random.nextDouble(minLat, maxLat) to Random.nextDouble(minLon, maxLon) }
                .toMutableMap()
        track()
    }

    @Scheduled(initialDelay = 5000, fixedRate = 250)
    fun generateTracks() {
        calculateNewCoordinates()
        track()
    }

    private fun calculateNewCoordinates() {
        userCoordinates.replaceAll { _, coordinates -> coordinates.nextCoordinates() }
    }

    private fun track() {
        val now = LocalDateTime.now()
        userCoordinates
                .map { (userId, coordinates) -> UserCoordinatesDto(
                        userId = userId,
                        timestamp = now,
                        coordinates = coordinates)
                }.forEach {
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

    private fun Pair<Double, Double>.nextCoordinates() = first.nextCoordinate() to second.nextCoordinate()

    private fun Double.nextCoordinate() = this + (Math.random() - 0.5) * maxStep
}