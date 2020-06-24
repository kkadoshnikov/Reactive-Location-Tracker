package com.gmail.kadoshnikovkirill.fake.tracks.generator.core.impl

import com.gmail.kadoshnikovkirill.fake.tracks.generator.UserCoordinatesDto
import com.gmail.kadoshnikovkirill.fake.tracks.generator.core.UserCoordinatesGenerator
import java.time.LocalDateTime
import kotlin.random.Random

class RandomUserCoordinatesGenerator(
        private val userId: Long,
        initialLat: Double,
        initialLon: Double,
        private val maxStep: Double): UserCoordinatesGenerator {

    private var lat = initialLat
    private var lon = initialLon

    override fun getCoordinates(): UserCoordinatesDto {
        changeCoordinates()
        return UserCoordinatesDto(
                userId = userId,
                timestamp = LocalDateTime.now(),
                lat = lat.toFloat(),
                lon = lon.toFloat())
    }

    private fun changeCoordinates() {
        lat += calculateStep()
        lon += calculateStep()
    }

    private fun calculateStep() = Random.nextDouble(-maxStep, maxStep)
}