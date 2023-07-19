package io.dialob.client.spi.support;

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

import io.dialob.client.api.DialobErrorHandler.DialobClientException;


public class DialobAssert {
  public static class DialobAssertException extends IllegalArgumentException implements DialobClientException {
    private static final long serialVersionUID = 6305063707279384796L;
    public DialobAssertException(String s) {
      super(s);
    }
  }

  
  public static void notNull(Object object, Supplier<String> message) {
    if (object == null) {
      throw new DialobAssertException(getMessage(message));
    }
  }
  public static void notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new DialobAssertException(getMessage(message));
    }
  }
  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new DialobAssertException(getMessage(message));
    }
  }
  private static String getMessage(Supplier<String> supplier) {
    return (supplier != null ? supplier.get() : null);
  }

}
