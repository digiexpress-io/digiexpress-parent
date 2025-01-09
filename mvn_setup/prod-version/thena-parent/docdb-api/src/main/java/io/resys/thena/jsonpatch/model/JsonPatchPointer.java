package io.resys.thena.jsonpatch.model;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import io.resys.thena.jsonpatch.visitors.ApplyPatch.JsonPatchException;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.core.json.pointer.impl.JsonPointerImpl;
import lombok.Getter;

public class JsonPatchPointer {
  public static final Integer APPEND_TO_ARRAY = Integer.MIN_VALUE;
  private static final Pattern VALID_POINTER_IN_ARRAY_PATTEN = Pattern.compile("-|0|(?:[1-9][0-9]*)");
  private static final Pattern VALID_POINTER_PATTERN = JsonPointerImpl.VALID_POINTER_PATTERN;
  private final URI uri;
  private final List<JsonPatchToken> tokens;
  
  public JsonPatchPointer() {
    this.uri = URI.create("#");
    this.tokens = Collections.emptyList();
  }
  public JsonPatchPointer(List<JsonPatchToken> tokens) {
    this.uri = URI.create("#");
    this.tokens = Collections.unmodifiableList(tokens);
  }
  public JsonPatchPointer(JsonPointer src) {
    this.uri = src.toURI();
    this.tokens = JsonPatchToken.decodeAll(src.toString());
  }
  private JsonPatchPointer(URI uri, List<JsonPatchToken> tokens) {
    this.uri = uri;
    this.tokens = tokens;
  }

  public JsonPatchPointer append(String path) {
    return new JsonPatchPointer(this.uri, ImmutableList.<JsonPatchToken>builder()
        .addAll(tokens)
        .add(JsonPatchToken.decodeOne(path))
        .build());
  }

  public JsonPatchPointer append(int path) {
    return this.append(String.valueOf(path));
  }

  public JsonPatchToken getLastPointer() {
    if (this.isRootPointer()) {
      return null;
    }
    return tokens.get(tokens.size() - 1);
  }
  public static JsonPatchPointer create() {
    return new JsonPatchPointer();
  }
  public List<JsonPatchToken> getTokens() {
    return tokens;
  }
  public JsonPatchToken getToken(int position) {
    return this.tokens.get(position);
  }
  public JsonPatchPointer withToken(int position, String value) {
    final var newTokens = new ArrayList<>(this.tokens);
    newTokens.set(position, JsonPatchToken.decodeOne(value));
    return new JsonPatchPointer(newTokens);
  }

  public boolean isRootPointer() {
    return tokens.size() == 0;
  }
  
  @Override
  public String toString() {
    if (isRootPointer()) {
      return "";
    } else {
      return "/" + String.join("/", tokens.stream().map(JsonPatchToken::encode).collect(Collectors.toList()));
    }
  }

  public static class JsonPatchToken {
    @Getter
    private final String token;
    private Boolean isArrayIndex;
    private Integer arrayIndex;
    private JsonPatchToken(String token) {
      this.token = token;
    }
    public static JsonPatchToken decodeOne(String path) {
      final var token = path
          .replace("~1", "/") // https://tools.ietf.org/html/rfc6901#section-4
          .replace("~0", "~");
      return new JsonPatchToken(token);
    }
    public static List<JsonPatchToken> decodeAll(String pointer) {
      if (pointer == null || "".equals(pointer)) {
        return Collections.emptyList();
      }
      if (VALID_POINTER_PATTERN.matcher(pointer).matches()) {
        return Arrays.stream(pointer.split("\\/", -1))
            .skip(1) 
            .map(value -> JsonPatchToken.decodeOne(value))
            .toList();
      }
      throw new JsonPatchException("Invalid json pointer: '%s'".formatted(pointer));
    }
    
    public String encode() {
      return token.replace("~", "~0").replace("/", "~1");
    }

    public boolean isArrayAppend() {
      return getIndex() == APPEND_TO_ARRAY;
    }
    
    public boolean isArrayIndex() {
      if(isArrayIndex != null) {
        return isArrayIndex;
      }
      final var matcher = VALID_POINTER_IN_ARRAY_PATTEN.matcher(token);
      isArrayIndex = matcher.matches();
      if(isArrayIndex) {
        arrayIndex = matcher.group().equals("-") ? APPEND_TO_ARRAY : Integer.parseInt(matcher.group());
      }
      return isArrayIndex;
    }

    public int getIndex() {
      if (!isArrayIndex()) {
        throw new JsonPatchException("Invalid json array pointer: '%s'".formatted(token));
      } 
      return arrayIndex;
    }
    @Override
    public int hashCode() {
      return Objects.hash(token);
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      JsonPatchToken other = (JsonPatchToken) obj;
      return Objects.equals(token, other.token);
    }
  }
}
