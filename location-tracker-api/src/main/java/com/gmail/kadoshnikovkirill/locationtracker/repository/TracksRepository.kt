package com.gmail.kadoshnikovkirill.locationtracker.repository

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track
import com.gmail.kadoshnikovkirill.locationtracker.domain.TrackKey
import io.micrometer.core.annotation.Timed
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Repository
interface TracksRepository : ReactiveCassandraRepository<Track, TrackKey> {
    @Timed("location.save")
    override fun <S : Track> save(entity: S): Mono<S>
    fun findByKeyUserId(userId: Long): Flux<Track>
    fun findByKeyUserIdAndKeyHr(userId: Long, hr: LocalDateTime): Flux<Track>
    @JvmDefault
    fun findByKeyUserIdAndPeriod(userId: Long, hours: Int): Flux<Track> {
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
        return Flux.fromIterable((0 until hours.toLong())
                .map { now.minusHours(it) })
                .flatMap { findByKeyUserIdAndKeyHr(userId, it) }
    }
}