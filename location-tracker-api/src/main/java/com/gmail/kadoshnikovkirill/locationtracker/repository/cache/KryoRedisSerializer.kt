package com.gmail.kadoshnikovkirill.locationtracker.repository.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayOutputStream;

public class KryoRedisSerializer<T> implements RedisSerializer<T> {

    private KryoPool kryoPool = new KryoPool.Builder(Kryo::new).softReferences().build();

    @Override
    public byte[] serialize(final T o) throws SerializationException {
        return kryoPool.run(kryo -> {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Output output = new Output(stream);
            kryo.writeClassAndObject(output, o);
            output.close();
            return stream.toByteArray();
        });
    }

    @Override
    public T deserialize(final byte[] bytes) throws SerializationException {
        // both not found or "null" value will return "null"
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return kryoPool.run(kryo -> {
            Input input = new Input(bytes);
            @SuppressWarnings("unchecked")
            T o = (T) kryo.readClassAndObject(input);
            input.close();
            return o;
        });
    }
}
