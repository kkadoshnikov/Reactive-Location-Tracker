package com.gmail.kadoshnikovkirill.locationtracker.domain

import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table

/**
 *
 */
@Table("tracks")
data class Track(
        @PrimaryKey
        val key: TrackKey,
        @Column
        val lat: Float,
        @Column
        val lon: Float,
        @Column("country_code")
        val countryCode: String? = null,
        @Column("postal_code")
        val postalCode: Int? = null,
        @Column
        val country: String? = null,
        @Column
        val region: String? = null,
        @Column
        val city: String? = null,
        @Column
        val street: String? = null,
        @Column
        val house: String? = null
)