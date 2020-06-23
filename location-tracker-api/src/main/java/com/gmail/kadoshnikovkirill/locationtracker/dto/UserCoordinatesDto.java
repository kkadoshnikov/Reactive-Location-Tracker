package com.gmail.kadoshnikovkirill.locationtracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCoordinatesDto {

    private final Long userId;
    private final Float lat;
    private final Float lon;
    private final LocalDateTime timestamp;
}
