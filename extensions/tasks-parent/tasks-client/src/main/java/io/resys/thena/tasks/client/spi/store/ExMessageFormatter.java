package io.resys.thena.tasks.client.spi.store;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.Arrays;
import java.util.stream.Collectors;

import io.resys.thena.tasks.client.spi.store.DocumentStoreException.DocumentExceptionMsg;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExMessageFormatter {
  private final String code;
  private final JsonObject target;
  private final DocumentExceptionMsg[] msg;
  private final StringBuilder result = new StringBuilder(System.lineSeparator()); 
  
  public String format() {
    result.append("Store operation failed with:").append(System.lineSeparator());
    visitCode();
    visitTarget();
    for(final var message : msg) {
      visitMessage(message);
    }
    return result.toString();
  }
  
  private void visitTarget() {
    if(target != null) {
      result.append("  - entity id: ").append("'" + target.getString("id") + "'").append(System.lineSeparator());
    }
  }
  
  private void visitMessage(DocumentExceptionMsg m) {
    result
    .append("  - msg id: '").append(m.getId()).append("'").append(System.lineSeparator())
    .append("  - msg value: '").append(m.getValue()).append("'").append(System.lineSeparator())
    .append("  - msg additional info: ").append(System.lineSeparator());
  
    for(final var arg : m.getArgs()) {
      final var nested = Arrays.asList(arg.trim()
          .split(System.lineSeparator())).stream()
          .map(n -> n.trim())
          .filter(n -> !n.isEmpty())
          .map(n -> {
            if(n.startsWith("-")) {
              return n.substring(1).trim();
            }
            return n;
          })
          .collect(Collectors.toList());
      
      if(!nested.isEmpty()) {
        result.append("    - ").append(nested.get(0)).append(System.lineSeparator());
      } 
      
      for(int index = 1; index < nested.size(); index++) {
        result.append("      - ").append(nested.get(index)).append(System.lineSeparator());
      }
    }
  }
  
  private void visitCode() {
    result.append("  - code: ").append("'" + code + "'").append(System.lineSeparator());
  } 
}
