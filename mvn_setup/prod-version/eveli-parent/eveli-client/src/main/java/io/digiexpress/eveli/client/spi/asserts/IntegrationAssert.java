package io.digiexpress.eveli.client.spi.asserts;

/*-
 * #%L
 * eveli-client
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

import java.util.function.Supplier;

public class IntegrationAssert {
  public static void notNull(Object object, Supplier<String> message) {
    if (object == null) {
      throw new IntegrationException(getMessage(message));
    }
  }
  public static IntegrationException fail(Supplier<String> message) {
    return new IntegrationException(getMessage(message));
  }
  public static IntegrationException fail(Exception e) {
    return new IntegrationException(e.getMessage(), e);
  }
  public static void notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new IntegrationException(getMessage(message));
    }
  }
  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new IntegrationException(getMessage(message));
    }
  }
  private static String getMessage(Supplier<String> supplier) {
    return (supplier != null ? supplier.get() : null);
  }

  public static class IntegrationException extends RuntimeException {

    private static final long serialVersionUID = 1781444267360040922L;

    public IntegrationException(String message, Throwable cause) {
      super(message, cause);
    }

    public IntegrationException(String message) {
      super(message);
    }
  }

}
