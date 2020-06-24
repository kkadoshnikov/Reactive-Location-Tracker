package com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates.impl

import com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates.UserCoordinatesGenerator
import com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates.UserCoordinatesGeneratorFactory
import com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates.UserIdSequence
import org.springframework.stereotype.Component
import kotlin.random.Random.Default.nextDouble

@Component
class RandomUserCoordinatesGeneratorFactory(
        private val userIdSequence: UserIdSequence
): UserCoordinatesGeneratorFactory {

    //ToDo: move to configuration
    private val minLat = 54.900820
    private val maxLat = 55.0005
    private val minLon = 73.283246
    private val maxLon = 73.490083
    private val maxStep = 0.00002

    override fun createSingle(): UserCoordinatesGenerator {
        val userId = userIdSequence.next()
        val initialLat = nextDouble(minLat, maxLat)
        val initialLon = nextDouble(minLon, maxLon)
        return RandomUserCoordinatesGenerator(userId, initialLat, initialLon, maxStep)
    }
}