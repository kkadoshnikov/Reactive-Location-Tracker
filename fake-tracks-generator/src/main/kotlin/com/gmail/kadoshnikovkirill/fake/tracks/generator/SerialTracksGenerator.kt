package com.gmail.kadoshnikovkirill.fake.tracks.generator

import com.gmail.kadoshnikovkirill.fake.tracks.generator.clients.TrackerClient
import com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates.UserCoordinatesGeneratorFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("serial.userCount")
class SerialTracksGenerator(
        @Value("\${serial.userCount}")
        private val userCount: Int,
        userCoordinatesGeneratorFactory: UserCoordinatesGeneratorFactory,
        private val trackerClient: TrackerClient
) {

    private val fakeUsers = userCoordinatesGeneratorFactory.createList(userCount)

    init {
        sendTracks() // Send first time with empty cache
    }

    // We need initial delay because first time takes more time (due to empty cache)
    @Scheduled(initialDelay = INIT_CACHE_DELAY, fixedRate = DELAY)
    fun generateTracks() {
        sendTracks()
    }

    private fun sendTracks() {
        fakeUsers.map { it.getCoordinates() }
                .forEach(trackerClient::sendTrack)
    }

    companion object {
        private const val INIT_CACHE_DELAY = 5000L
        private const val DELAY = 250L
    }
}