package com.gmail.kadoshnikovkirill.locationtracker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDateTime;

@PrimaryKeyClass
@Data
@AllArgsConstructor
public class TrackKey {

    @PrimaryKeyColumn(
            name = "hr", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private LocalDateTime hr;
    @PrimaryKeyColumn(
            name = "user_id",
            ordinal = 1,
            type = PrimaryKeyType.PARTITIONED)
    private Long userId;
    @PrimaryKeyColumn(
            name = "ts",
            ordinal = 2,
            type = PrimaryKeyType.PARTITIONED)
    private LocalDateTime ts;

}
