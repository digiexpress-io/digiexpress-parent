package io.resys.hdes.resource.editor.spi.support;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

/*-
 * #%L
 * wrench-assets-datatype
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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

import org.apache.commons.codec.binary.Hex;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepoAssert {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepoAssert.class);
  
  @Value.Immutable
  public interface StatusMessage {
    String getId();
    String getValue();
    Optional<String> getLogCode();
    Optional<String> getLogStack();
  }

  public static String exceptionHash(String msg) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.reset();
      md5.update(msg.getBytes(Charset.forName("UTF-8")));
      byte[] digest = md5.digest();
      return Hex.encodeHexString(digest);
    } catch (NoSuchAlgorithmException ex) {
      // Fall back to just hex timestamp in this improbable situation
      LOGGER.warn("MD5 Digester not found, falling back to timestamp hash", ex);
      long timestamp = System.currentTimeMillis();
      return Long.toHexString(timestamp);
    }
  }

  public static void notEmptyAtLeastOne(Supplier<String> message, String ...fields) {
    for(String field : fields) {
      if (field != null && !field.isBlank()) {
        return;
      }
    }
    throw new ReRepoException(getMessage(message));
  }
  public static void notEmptyAll(Supplier<String> message, String ...fields) {
    for(String field : fields) {
      if (field == null || field.isBlank()) {
        throw new ReRepoException(getMessage(message));
      }
    }
  }
  public static void notNull(Object object, Supplier<String> message) {
    if (object == null) {
      throw new ReRepoException(getMessage(message));
    }
  }
  public static void notEmpty(List<?> object, Supplier<String> message) {
    if (object == null || object.isEmpty()) {
      throw new ReRepoException(getMessage(message));
    }
  }  
  public static void notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new ReRepoException(getMessage(message));
    }
  }
  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new ReRepoException(getMessage(message));
    }
  }
  public static void fail(Supplier<String> message) {
    throw new ReRepoException(getMessage(message));
  }
  private static String getMessage(Supplier<String> supplier) {
    return (supplier != null ? supplier.get() : null);
  }

}
