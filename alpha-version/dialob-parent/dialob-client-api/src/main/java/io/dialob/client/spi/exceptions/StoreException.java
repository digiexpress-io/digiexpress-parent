package io.dialob.client.spi.exceptions;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.dialob.client.api.DialobStore.StoreEntity;
import io.dialob.client.api.DialobStore.StoreExceptionMsg;

public class StoreException extends RuntimeException {

  private static final long serialVersionUID = 7058468238867536222L;

  private final String code;
  private final Optional<StoreEntity> target;
  private final List<StoreExceptionMsg> messages = new ArrayList<>();
  
  public StoreException(Exception e, String code, StoreEntity target) {
    super(e.getMessage(), e);
    this.code = code;
    this.target = Optional.ofNullable(target);
  }

  public StoreException(Exception e, String code, StoreEntity target, StoreExceptionMsg ... msg) {
    super(e.getMessage(), e);
    this.code = code;
    this.target = Optional.ofNullable(target);
    this.messages.addAll(Arrays.asList(msg));
  }
    
  public StoreException(String code, StoreEntity target) {
    super();
    this.code = code;
    this.target = Optional.ofNullable(target);
  }

  public StoreException(String code, StoreEntity target, StoreExceptionMsg ... msg) {
    super(formatMessages(code, target, msg));
    this.code = code;
    this.target = Optional.ofNullable(target);
    this.messages.addAll(Arrays.asList(msg));
  }
  
  private static String formatMessages(String code, StoreEntity target, StoreExceptionMsg ... msg) {
    final var builder = new StringBuilder()
        .append(System.lineSeparator())
        .append("Store operation failed with:").append(System.lineSeparator())
        .append("  - code: ").append("'" + code + "'").append(System.lineSeparator());
    
    
    if(target != null) {
      builder.append("  - entity id: ").append("'" + target.getId() + "'").append(System.lineSeparator());
    }
    
    
    for(final var m : msg) {
      builder
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
          builder.append("    - ").append(nested.get(0)).append(System.lineSeparator());
        } 
        
        for(int index = 1; index < nested.size(); index++) {
          builder.append("      - ").append(nested.get(index)).append(System.lineSeparator());
        }
      }
    }
    
    return builder.toString();
  }
  
  
  
  public String getCode() {
    return code;
  }
  public Optional<StoreEntity> getTarget() {
    return target;
  }
  public List<StoreExceptionMsg> getMessages() {
    return messages;
  }
}
