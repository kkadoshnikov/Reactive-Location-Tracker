package com.gmail.kadoshnikovkirill.locationtracker

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track
import com.gmail.kadoshnikovkirill.locationtracker.dto.UserCoordinatesDto
import com.gmail.kadoshnikovkirill.locationtracker.repository.TracksRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrackControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var repository: TracksRepository

    @Test
    fun testTrackLocation() {
        val dto = UserCoordinatesDto(2L, 2.4f, 3.5f, LocalDateTime.now())
        webTestClient.post()
                .uri("/tracks")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(dto), Track::class.java)
                .exchange()
                .expectStatus().isCreated
    }

    @Test
    fun testGetAllLocations() {
        webTestClient.get()
                .uri { uriBuilder: UriBuilder ->
                    uriBuilder.path("/tracks")
                            .queryParam("userId", 2)
                            .build()
                }
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(UserCoordinatesDto::class.java)
    }
}