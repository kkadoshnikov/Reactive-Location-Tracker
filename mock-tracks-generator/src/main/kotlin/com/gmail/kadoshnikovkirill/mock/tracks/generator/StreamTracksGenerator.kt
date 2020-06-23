package com.gmail.kadoshnikovkirill.mock.tracks.generator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@ConditionalOnProperty("stream.userCount")
public class StreamTracksGenerator {

    private final WebClient webClient;
    private final double minLat = 54.900820;
    private final double maxLat = 55.0005;
    private final double minLon = 73.283246;
    private final double maxLon = 73.490083;
    private final double maxStep = 0.00002;
    @Value("${stream.userCount}")
    private int userCount;
    private Map<Long, Tuple2<Double, Double>> userCoordinates;

    public StreamTracksGenerator() {

        this.webClient = WebClient
                .builder()
                .baseUrl("http://localhost:8080/tracks")
                .build();
    }

    @PostConstruct
    public void init() {
        userCoordinates = LongStream.range(0, userCount).boxed().collect(Collectors.toMap(
            Function.identity(),
            i -> Tuples.of(randomInRange(minLat, maxLat), randomInRange(minLon, maxLon))));
        LocalDateTime now = LocalDateTime.now();

        trackInitialState(now);
    }

    private void trackInitialState(LocalDateTime now) {
        userCoordinates.forEach((i, coordinates) -> webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(toUserCoordinates(now, i, coordinates)))
                .exchange()
                .map(ClientResponse::statusCode)
                .log()
                .subscribe());
    }

    /**
     *
     * fixedRate = Long.MAX_VALUE isn't a good solution, but it's OK for mock.
     */
    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void startTracksStream() {
        userCoordinates.replaceAll((i, coordinates) -> makeStep(coordinates));
        userCoordinates.keySet().forEach(userId -> webClient.post()
                .uri("/stream")
                .contentType(MediaType.APPLICATION_STREAM_JSON)
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .body(BodyInserters.fromPublisher(generateCoordinatesFlux(userId), UserCoordinatesDto.class))
                .exchange()
                .map(ClientResponse::statusCode)
                .log()
                .subscribe());
    }

    private Flux<UserCoordinatesDto> generateCoordinatesFlux(Long userId) {
        return Flux.<UserCoordinatesDto>generate(sync -> {
            Tuple2<Double, Double> newCoordinates = userCoordinates
                    .computeIfPresent(userId, (i, coordinates) -> makeStep(coordinates));
            sync.next(toUserCoordinates(LocalDateTime.now(), userId, newCoordinates));
        }).delayElements(Duration.ofMillis(100));
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
