package com.gmail.kadoshnikovkirill.mock.tracks.generator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@ConditionalOnProperty("serial.userCount")
public class SerialTracksGenerator {

    private final WebClient webClient;
    private final double minLat = 54.900820;
    private final double maxLat = 55.0005;
    private final double minLon = 73.283246;
    private final double maxLon = 73.490083;
    private final double maxStep = 0.00002;
    @Value("${serial.userCount}")
    private int userCount;
    private Map<Long, Tuple2<Double, Double>> userCoordinates;

    public SerialTracksGenerator() {
        this.webClient = WebClient
            .builder()
            .baseUrl("localhost:8080/tracks")
            .build();
        track();
    }

    @Scheduled(initialDelay = 5000, fixedRate = 250)
    public void generateTracks() {
        userCoordinates = LongStream.range(0, userCount).boxed().collect(Collectors.toMap(
            Function.identity(),
            i -> Tuples.of(randomInRange(minLat, maxLat), randomInRange(minLon, maxLon))));
        userCoordinates.replaceAll((i, coordinates) -> makeStep(coordinates));
        track();
    }

    private void track() {
        LocalDateTime now = LocalDateTime.now();
        userCoordinates.forEach((i, coordinates) -> webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(toUserCoordinates(now, i, coordinates)))
                .exchange()
                .map(ClientResponse::statusCode)
                .log()
                .subscribe());
    }

    private Double randomInRange(double min, double max) {
        return min + Math.random() * (max - min);
    }

    private Tuple2<Double, Double> makeStep(Tuple2<Double, Double> currCoordinated) {
        return currCoordinated.mapT1(x -> x + (Math.random() - 0.5) * maxStep)
                .mapT2(x -> x + (Math.random() - 0.5) * maxStep);
    }

    private UserCoordinatesDto toUserCoordinates(LocalDateTime now, Long i, Tuple2<Double, Double> coordinates) {
        return new UserCoordinatesDto(i, coordinates.getT1().floatValue(), coordinates.getT2().floatValue(), now);
    }
}
