package com.gmail.kadoshnikovkirill.locationtracker.provider

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto
import com.gmail.kadoshnikovkirill.reactive.metrics.MeteredMono
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class YandexLocationProvider(
        @Value("\${geocode-maps.apiKey}") private val apiKey: String,
        @Value("\${geocode-maps.lang}") private val lang: String,
        @Value("\${geocode-maps.timeout}") private val timeout: Int
) : LocationProvider {

    private val webClient: WebClient = WebClient.create("https://geocode-maps.yandex.ru/1.x/")

    @MeteredMono(value = "location.yandex.get", percentiles = [0.75, 0.95, 0.98, 0.99, 0.999])
    override fun getByCoordinates(lat: Float, lon: Float): Mono<LocationDto> {
        return webClient.get()
                .uri { it
                        .queryParam("format", "json")
                        .queryParam("apikey", apiKey)
                        .queryParam("sco", "latlong")
                        .queryParam("lang", lang)
                        .queryParam("result", 1)
                        .queryParam("geocode", "$lat,$lon")
                        .build()
                }.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(LocationDto::class.java)
                .timeout(Duration.ofMillis(timeout.toLong()))
    }

}