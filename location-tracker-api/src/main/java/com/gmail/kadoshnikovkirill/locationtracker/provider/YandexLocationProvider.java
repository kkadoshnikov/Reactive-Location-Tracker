package com.gmail.kadoshnikovkirill.locationtracker.provider;

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto;
import com.gmail.kadoshnikovkirill.reactive.metrics.MeteredMono;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class YandexLocationProvider implements LocationProvider {

    private final WebClient webClient;
    private final String apiKey;
    private final String lang;
    private final Integer timeout;

    public YandexLocationProvider(
            @Value("${geocode-maps.apiKey}") String apiKey,
            @Value("${geocode-maps.lang}") String lang,
            @Value("${geocode-maps.timeout}") Integer timeout
    ) {
        this.apiKey = apiKey;
        this.lang = lang;
        this.timeout = timeout;
        this.webClient = WebClient
                .builder()
                .baseUrl("https://geocode-maps.yandex.ru/1.x/")
                .build();
    }

    @Override
    @MeteredMono(value = "location.yandex.get", percentiles = {0.75, 0.95, 0.98, 0.99, 0.999})
    public Mono<LocationDto> getByCoordinates(float lat, float lon) {
        return webClient.get().uri(uriBuilder -> uriBuilder
                .queryParam("format", "json")
                .queryParam("apikey", apiKey)
                .queryParam("sco", "latlong")
                .queryParam("lang", lang)
                .queryParam("result", 1)
                .queryParam("geocode", lat + "," + lon).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(LocationDto.class)
                .timeout(Duration.ofMillis(timeout))
                .flatMap(it -> {
                    if (it.getCountryCode() != null) {
                        return Mono.just(it);
                    }
                    return Mono.delay(Duration.ofMillis(1000)).flatMap(id -> getByCoordinates(lat, lon));
                });
    }
}
