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

public class ProcessAssert {
  public static void notNull(Object object, Supplier<String> message) {
    if (object == null) {
      throw new ProcessException(getMessage(message));
    }
  }
  public static void notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new ProcessException(getMessage(message));
    }
  }
  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new ProcessException(getMessage(message));
    }
  }
  private static String getMessage(Supplier<String> supplier) {
    return (supplier != null ? supplier.get() : null);
  }

  public static class ProcessException extends RuntimeException {

    private static final long serialVersionUID = 1781444267360040922L;

    public ProcessException(String message, Throwable cause) {
      super(message, cause);
    }

    public ProcessException(String message) {
      super(message);
    }
  }

}
