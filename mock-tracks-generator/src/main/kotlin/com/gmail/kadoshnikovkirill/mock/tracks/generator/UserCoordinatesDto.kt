package com.gmail.kadoshnikovkirill.mock.tracks.generator;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCoordinatesDto {

    private final Long userId;
    private final Float lat;
    private final Float lon;
    private final LocalDateTime timestamp;
}
