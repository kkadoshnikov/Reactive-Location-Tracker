package com.gmail.kadoshnikovkirill.fake.tracks.generator.core.impl

import com.gmail.kadoshnikovkirill.fake.tracks.generator.core.UserIdSequence
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class InMemoryUserIdSequence: UserIdSequence {

    private val idSequence = AtomicLong(0)

    override fun next() = idSequence.incrementAndGet()
}