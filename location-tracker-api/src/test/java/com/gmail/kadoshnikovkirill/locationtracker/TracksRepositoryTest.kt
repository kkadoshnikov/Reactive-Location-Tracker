package com.gmail.kadoshnikovkirill.locationtracker

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track
import com.gmail.kadoshnikovkirill.locationtracker.domain.TrackKey
import com.gmail.kadoshnikovkirill.locationtracker.repository.TracksRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@RunWith(SpringRunner::class)
@SpringBootTest
class TracksRepositoryTest {
    @Autowired
    private lateinit var repository: TracksRepository
    private val now = LocalDateTime.now()
    private val hr = now.truncatedTo(ChronoUnit.HOURS)

    @Before
    fun setUp() {
        val deleteAndInsert: Flux<Track> = repository.deleteAll()
                .thenMany(repository.saveAll(Flux.just(
                        Track(
                                key = TrackKey(hr, 1L, hr),
                                lat = 10.4f,
                                lon = 10.4f),
                        Track(
                                key = TrackKey(hr, 1L, hr.plusMinutes(2)),
                                lat = 11.4f,
                                lon = 12.4f),
                        Track(
                                key = TrackKey(hr, 1L, hr.plusMinutes(3)),
                                lat = 12.4f,
                                lon = 11.4f),
                        Track(
                                key = TrackKey(hr.minusDays(2), 1L, hr.plusMinutes(5)),
                                lat = 10.6f,
                                lon = 10.2f))))
        StepVerifier
                .create(deleteAndInsert)
                .expectNextCount(4)
                .verifyComplete()
    }

    @Test
    fun testSaveAll() {
        val saveAndCount: Mono<Long> = repository.count()
                .thenMany(repository
                        .saveAll(Flux.just(
                                Track(
                                        key = TrackKey(hr, 1L, hr),
                                        lat = 10.2f,
                                        lon = 10.1f),
                                Track(
                                        key = TrackKey(hr, 1L, hr),
                                        lat = 11.9f,
                                        lon = 12.5f))))
                .last()
                .flatMap{ repository.count() }
        StepVerifier
                .create(saveAndCount)
                .expectNext(6L)
                .verifyComplete()
    }

    @Test
    fun testFindByKeyUserIdAndPeriod() {
        StepVerifier
                .create(repository.findByKeyUserIdAndPeriod(1L, 24))
                .expectNextCount(3)
                .verifyComplete()
    }
}