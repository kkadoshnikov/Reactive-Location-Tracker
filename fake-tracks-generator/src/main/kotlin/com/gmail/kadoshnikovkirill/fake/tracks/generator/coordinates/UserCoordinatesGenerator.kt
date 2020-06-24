package com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates

import com.gmail.kadoshnikovkirill.fake.tracks.generator.model.UserCoordinatesDto

interface UserCoordinatesGenerator {
    fun getCoordinates(): UserCoordinatesDto
}