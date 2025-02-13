package io.resys.hdes.compiler.spi.spec;

/*-
 * #%L
 * hdes-compiler
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;

public class JavaSpecUtil {
  
  
  public static String methodCall(String name) {
    //turns given ref into "get + (N)ame + ()"
    String[] src = name.split("\\.");
    StringBuilder result = new StringBuilder();
    for(String target : src) {
      if(result.length() > 0) {
        result.append(".");
      }
      result.append("get").append(capitalize(target)).append("()");
    }
    return result.toString();
  }
  
  public static String getMethodName(String name) {
    //turns given ref into "get + (N)ame"
    return new StringBuilder()
        .append("get")
        .append(capitalize(name))
        .toString();
  }

  public static String capitalize(String name) {
    return new StringBuilder()
        .append(name.substring(0, 1).toUpperCase())
        .append(name.length() == 1 ? "" : name.substring(1))
        .toString();
  }
  public static String decapitalize(String name) {
    return new StringBuilder()
        .append(name.substring(0, 1).toLowerCase())
        .append(name.length() == 1 ? "" : name.substring(1))
        .toString();
  }

  public static Class<?> type(ScalarType entry) {
    switch (entry) {
    case BOOLEAN:
      return boolean.class;
    case DATE:
      return LocalDate.class;
    case DATETIME:
      return LocalDateTime.class;
    case TIME:
      return LocalTime.class;
    case DECIMAL:
      return BigDecimal.class;
    case INTEGER:
      return Integer.class;
    case STRING:
      return String.class;
    default:
      throw new IllegalArgumentException("Unimplemented type: " + entry + "!");
    }
  }
}
