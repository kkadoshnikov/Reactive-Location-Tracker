package com.gmail.kadoshnikovkirill.locationtracker.domain

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import java.time.LocalDateTime

@PrimaryKeyClass
class TrackKey(
    @PrimaryKeyColumn(name = "hr", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    val hr: LocalDateTime,
    @PrimaryKeyColumn(name = "user_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    val userId: Long,
    @PrimaryKeyColumn(name = "ts", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
    val ts: LocalDateTime)