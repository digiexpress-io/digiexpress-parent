package io.resys.thena.docdb.support;

import java.util.List;
import java.util.Optional;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.function.Supplier;

import io.resys.thena.docdb.api.exceptions.RepoException;

public class RepoAssert {
  private static final String NAME_PATTER = "^([a-zA-Z0-9 +_/-]|\\\\\\\\)+";
  
  public static String isName(String value, Supplier<String> message) {
    RepoAssert.isTrue(value.matches(NAME_PATTER), () -> message.get() + " => Valid name pattern: '" + NAME_PATTER + "'!");
    return value;
  }
  public static String notEmptyIfDefined(Optional<String> src, Supplier<String> message) {
    if(src == null) {
      return null;
    }
    
    final var object = src.orElseGet(null);
    if (object == null || object.isBlank()) {
      throw new RepoException(getMessage(message));
    }
    return object;
  }
  public static List<String> notEmpty(List<String> object, Supplier<String> message) {
    if (object == null || object.isEmpty()) {
      throw new RepoException(getMessage(message));
    }
    return object;
  }
  public static String notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new RepoException(getMessage(message));
    }
    return object;
  }
  public static <T> T isNull(T object, Supplier<String> message) {
    if (object == null) {
      return object;
    }
    throw new RepoException(getMessage(message));
  }
  public static String isEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      return object;
    }
    throw new RepoException(getMessage(message));
  }
  public static <T> T notNull(T object, Supplier<String> message) {
    if (object == null) {
      throw new RepoException(getMessage(message));
    }
    return object;
  }
  public static void isTrue(boolean expression, Supplier<String> message, Object ...args) {
    if (!expression) {
      throw new RepoException(getMessage(message, args));
    }
  }
  private static String getMessage(Supplier<String> supplier, Object ... args) {
    return (supplier != null ? supplier.get().formatted(args) : null);
  }

  public static <T> T fail(String message) {
    throw new RepoException(getMessage(() -> message));
  }
}
