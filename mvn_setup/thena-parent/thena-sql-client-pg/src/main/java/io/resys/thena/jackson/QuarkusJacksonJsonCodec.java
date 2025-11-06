package io.resys.thena.jackson;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.VertxModule;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Same as io.quarkus.vertx.runtime.jackson.QuarkusJacksonJsonCodec
 * + additional modules
 */
public class QuarkusJacksonJsonCodec implements JsonCodec {

    private static final ObjectMapper mapper;
    // we don't want to create this unless it's absolutely necessary (and it rarely is)
    private static volatile ObjectMapper prettyMapper;

    static {
      mapper = new ObjectMapper();

      // Non-standard JSON but we allow C style comments in our JSON
      mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
      
      SimpleModule module = new VertxModule();
      module.addDeserializer(JsonObject.class, new JsonObjectDeserializer());
      module.addDeserializer(JsonArray.class, new JsonArrayDeserializer());


      mapper.registerModule(module);
      
      // additional modules
      mapper.registerModule(new JavaTimeModule());
      mapper.registerModule(new Jdk8Module());
      mapper.registerModule(new GuavaModule());
      
      
      // false = 2025-01-15T09:51:17.113535Z
      // true = 1736934713.942034000
      mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
    }

    private ObjectMapper prettyMapper() {
        if (prettyMapper == null) {
            prettyMapper = mapper.copy();
            prettyMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }
        return prettyMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T fromValue(Object json, Class<T> clazz) {
        T value = QuarkusJacksonJsonCodec.mapper.convertValue(json, clazz);
        if (clazz == Object.class) {
            value = (T) adapt(value);
        }
        return value;
    }

    @Override
    public <T> T fromString(String str, Class<T> clazz) throws DecodeException {
        return fromParser(createParser(str), clazz);
    }

    @Override
    public <T> T fromBuffer(Buffer buf, Class<T> clazz) throws DecodeException {
        return fromParser(createParser(buf), clazz);
    }

    @SuppressWarnings("deprecation")
    public static JsonParser createParser(Buffer buf) {
        try {
            return QuarkusJacksonJsonCodec.mapper.getFactory()
                    .createParser((InputStream) new ByteBufInputStream(buf.getByteBuf()));
        } catch (IOException e) {
            throw new DecodeException("Failed to decode:" + e.getMessage(), e);
        }
    }

    public static JsonParser createParser(String str) {
        try {
            return QuarkusJacksonJsonCodec.mapper.getFactory().createParser(str);
        } catch (IOException e) {
            throw new DecodeException("Failed to decode:" + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromParser(JsonParser parser, Class<T> type) throws DecodeException {
        T value;
        JsonToken remaining;
        try {
            value = QuarkusJacksonJsonCodec.mapper.readValue(parser, type);
            remaining = parser.nextToken();
        } catch (Exception e) {
            throw new DecodeException("Failed to decode:" + e.getMessage(), e);
        } finally {
            close(parser);
        }
        if (remaining != null) {
            throw new DecodeException("Unexpected trailing token");
        }
        if (type == Object.class) {
            value = (T) adapt(value);
        }
        return value;
    }

    @Override
    public String toString(Object object, boolean pretty) throws EncodeException {
        try {
            ObjectMapper mapper = pretty ? prettyMapper() : QuarkusJacksonJsonCodec.mapper;
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Buffer toBuffer(Object object, boolean pretty) throws EncodeException {
        try {
            ObjectMapper mapper = pretty ? prettyMapper() : QuarkusJacksonJsonCodec.mapper;
            return Buffer.buffer(mapper.writeValueAsBytes(object));
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage(), e);
        }
    }

    private static void close(Closeable parser) {
        try {
            parser.close();
        } catch (IOException ignore) {
        }
    }

    @SuppressWarnings("rawtypes")
    private static Object adapt(Object o) {
        try {
            if (o instanceof List) {
                List list = (List) o;
                return new JsonArray(list);
            } else if (o instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) o;
                return new JsonObject(map);
            }
            return o;
        } catch (Exception e) {
            throw new DecodeException("Failed to decode: " + e.getMessage());
        }
    }

    public static ObjectMapper mapper() {
      return mapper;
    }
}
