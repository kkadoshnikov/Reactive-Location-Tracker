package com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates

interface UserCoordinatesGeneratorFactory {
    fun createSingle() : UserCoordinatesGenerator
    fun createList(size: Int): List<UserCoordinatesGenerator> {
        return List(size) {
            createSingle()
        }
    }
}