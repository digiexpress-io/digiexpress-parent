package io.thestencil.client.spi.staticontent.support;

/*-
 * #%L
 * stencil-static-content
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

public class ParserAssert {

  public static void notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new StaticContentParserException(getMessage(message));
    }
  }
  public static void isEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      return;
    }
    throw new StaticContentParserException(getMessage(message));
  }
  public static void notNull(Object object, Supplier<String> message) {
    if (object == null) {
      throw new StaticContentParserException(getMessage(message));
    }
  }
  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new StaticContentParserException(getMessage(message));
    }
  }
  private static String getMessage(Supplier<String> supplier) {
    return (supplier != null ? supplier.get() : null);
  }
  
  public static class StaticContentParserException extends RuntimeException {
    private static final long serialVersionUID = -16242216446897210L;

    public StaticContentParserException(String message, Throwable cause) {
      super(message, cause);
    }

    public StaticContentParserException(String message) {
      super(message);
    }

    public StaticContentParserException(Throwable cause) {
      super(cause);
    }
  }

}
