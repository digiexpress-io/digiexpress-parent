package io.resys.hdes.client.spi.git;

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

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.spi.GitConfig.GitSerializer;

public class GitSerializerImpl implements GitSerializer {

  private final ObjectMapper objectMapper;
  private final TypeReference<List<AstCommand>> ref = new TypeReference<List<AstCommand>>() {};
  
  public GitSerializerImpl(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }

  @Override
  public List<AstCommand> read(String commands) {
    try {
      if(commands.startsWith("{")) {
        final var tree = objectMapper.readTree(commands);
        return objectMapper.convertValue(tree.get("commands"), ref);
      }
      
      return objectMapper.readValue(commands, ref);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public String write(List<AstCommand> commands) {
    try {
      return objectMapper.writeValueAsString(commands);
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
}
