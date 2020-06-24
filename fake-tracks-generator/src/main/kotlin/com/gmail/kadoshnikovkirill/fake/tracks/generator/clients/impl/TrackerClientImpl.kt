package com.gmail.kadoshnikovkirill.fake.tracks.generator.clients.impl

import com.gmail.kadoshnikovkirill.fake.tracks.generator.clients.TrackerClient
import com.gmail.kadoshnikovkirill.fake.tracks.generator.model.UserCoordinatesDto
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient

@Component
class TrackerClientImpl(@Value("\${tracker.url}") trackerUrl: String): TrackerClient {

    private val webClient: WebClient = WebClient.create(trackerUrl)

    override fun sendTrack(coordinates: UserCoordinatesDto) {
        webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(coordinates))
                .exchange()
                .map(ClientResponse::statusCode)
                .log()
                .subscribe()
    }

    override fun streamTracks(publisher: Publisher<UserCoordinatesDto>) {
        webClient.post()
                .uri("/stream")
                .contentType(MediaType.APPLICATION_STREAM_JSON)
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .body(BodyInserters.fromPublisher(publisher, UserCoordinatesDto::class.java))
                .exchange()
                .map(ClientResponse::statusCode)
                .log()
                .subscribe()
    }

}