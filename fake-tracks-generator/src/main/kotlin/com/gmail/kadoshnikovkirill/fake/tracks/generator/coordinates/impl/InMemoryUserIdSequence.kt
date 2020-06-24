package com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates.impl

import com.gmail.kadoshnikovkirill.fake.tracks.generator.coordinates.UserIdSequence
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class InMemoryUserIdSequence: UserIdSequence {

    private val idSequence = AtomicLong(0)

    override fun next() = idSequence.incrementAndGet()
}