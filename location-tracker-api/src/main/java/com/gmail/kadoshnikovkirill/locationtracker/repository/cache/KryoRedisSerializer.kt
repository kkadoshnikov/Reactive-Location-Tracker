package com.gmail.kadoshnikovkirill.locationtracker.repository.cache

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.pool.KryoFactory
import com.esotericsoftware.kryo.pool.KryoPool
import org.springframework.data.redis.serializer.RedisSerializer
import java.io.ByteArrayOutputStream

class KryoRedisSerializer<T> : RedisSerializer<T?> {
    private val kryoPool = KryoPool.Builder(KryoFactory { Kryo() }).softReferences().build()

    override fun serialize(o: T?): ByteArray {
        return kryoPool.run { kryo: Kryo ->
            val stream = ByteArrayOutputStream()
            val output = Output(stream)
            kryo.writeClassAndObject(output, o)
            output.close()
            stream.toByteArray()
        }
    }

    override fun deserialize(bytes: ByteArray): T? {
        // both not found or "null" value will return "null"
        return if (bytes == null || bytes.size == 0) {
            null
        } else kryoPool.run { kryo: Kryo ->
            val input = Input(bytes)
            val o = kryo.readClassAndObject(input) as T
            input.close()
            o
        }
    }
}