package com.gmail.kadoshnikovkirill.fake.tracks.generator

import com.gmail.kadoshnikovkirill.fake.tracks.generator.clients.TrackerClient
import com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates.UserCoordinatesGenerator
import com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates.UserCoordinatesGeneratorFactory
import com.gmail.kadoshnikovkirill.fake.tracks.generator.model.UserCoordinatesDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Duration

@Service
@ConditionalOnProperty("stream.userCount")
class StreamTracksGenerator(
        @Value("\${stream.userCount}")
        private val userCount: Int,
        userCoordinatesGeneratorFactory: UserCoordinatesGeneratorFactory,
        private val trackerClient: TrackerClient
) {

    private val fakeUsers = userCoordinatesGeneratorFactory.createList(userCount)

    init {
        trackInitialState()
    }

    private fun trackInitialState() {
        fakeUsers.map { it.getCoordinates() }
                .forEach(trackerClient::sendTrack)
    }

    // We need initial delay because first time takes more time (due to empty cache).
    // fixedRate = Long.MAX_VALUE isn't a good solution, but it's OK for fake.
    @Scheduled(initialDelay = INIT_CACHE_DELAY, fixedRate = Long.MAX_VALUE)
    fun startTracksStream() {
        fakeUsers.forEach {
            trackerClient.streamTracks(generateCoordinatesFlux(it))
        }
    }

    private fun generateCoordinatesFlux(fakeUser: UserCoordinatesGenerator): Flux<UserCoordinatesDto> {
        return Flux.generate<UserCoordinatesDto> {
            it.next(fakeUser.getCoordinates())
        }.delayElements(Duration.ofMillis(DELAY))
    }

    companion object {
        private const val INIT_CACHE_DELAY = 5000L
        private const val DELAY = 100L
    }
}