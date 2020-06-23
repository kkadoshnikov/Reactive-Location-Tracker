package com.gmail.kadoshnikovkirill.locationtracker;

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track;
import com.gmail.kadoshnikovkirill.locationtracker.domain.TrackKey;
import com.gmail.kadoshnikovkirill.locationtracker.repository.TracksRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.HOURS;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TracksRepositoryTest {

    @Autowired
    private TracksRepository repository;
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime hr = now.truncatedTo(HOURS);

    @Before
    public void setUp() {

        Flux<Track> deleteAndInsert = repository.deleteAll()
                .thenMany(repository.saveAll(Flux.just(
                        Track.builder()
                                .key(new TrackKey(hr, 1L, hr))
                                .lat(10.4f)
                                .lon(10.4f)
                                .build(),
                        Track.builder()
                                .key(new TrackKey(hr, 1L, hr.plusMinutes(2)))
                                .lat(11.4f)
                                .lon(12.4f)
                                .build(),
                        Track.builder()
                                .key(new TrackKey(hr, 1L, hr.plusMinutes(3)))
                                .lat(12.4f)
                                .lon(11.4f)
                                .build(),
                        Track.builder()
                                .key(new TrackKey(hr.minusDays(2), 1L, hr.minusDays(2).plusMinutes(5)))
                                .lat(10.6f)
                                .lon(10.2f)
                                .build())));

        StepVerifier
                .create(deleteAndInsert)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void testSaveAll() {
        Mono<Long> saveAndCount = repository.count()
                .thenMany(repository
                        .saveAll(Flux.just(
                                Track.builder()
                                        .key(new TrackKey(hr, 1L, hr.plusMinutes(32)))
                                        .lat(10.2f)
                                        .lon(10.1f)
                                        .build(),
                                Track.builder()
                                        .key(new TrackKey(hr, 1L, hr.plusMinutes(21)))
                                        .lat(11.9f)
                                        .lon(12.5f)
                                        .build())))
                .last()
                .flatMap(v -> repository.count());

        StepVerifier
                .create(saveAndCount)
                .expectNext(6L)
                .verifyComplete();
    }

    @Test
    public void testFindByKeyUserIdAndPeriod() {
        StepVerifier
                .create(repository.findByKeyUserIdAndPeriod(1L, 24))
                .expectNextCount(3)
                .verifyComplete();
    }
}
