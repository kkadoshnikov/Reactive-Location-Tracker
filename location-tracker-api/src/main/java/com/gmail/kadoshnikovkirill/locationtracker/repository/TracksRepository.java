package com.gmail.kadoshnikovkirill.locationtracker.repository;

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track;
import com.gmail.kadoshnikovkirill.locationtracker.domain.TrackKey;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

@Repository
public interface TracksRepository extends ReactiveCassandraRepository<Track, TrackKey> {

    @Override
    @Timed("location.save")
    <S extends Track> Mono<S> save(S entity);

    Flux<Track> findByKeyUserId(Long userId);

    Flux<Track> findByKeyUserIdAndKeyHr(Long userId, LocalDateTime hr);

    default Flux<Track> findByKeyUserIdAndPeriod(Long userId, Integer hours) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        return Flux.fromStream(IntStream.iterate(0, i -> ++i).limit(hours).boxed())
                .map(now::minusHours)
                .flatMap(hour -> findByKeyUserIdAndKeyHr(userId, hour));
    }
}
