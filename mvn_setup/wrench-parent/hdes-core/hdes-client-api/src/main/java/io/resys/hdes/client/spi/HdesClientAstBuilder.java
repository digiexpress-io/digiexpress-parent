package io.resys.hdes.client.spi;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;

import io.resys.hdes.client.api.HdesAstTypes;
import io.resys.hdes.client.api.HdesClient.AstBuilder;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;

public class HdesClientAstBuilder implements AstBuilder {
  private final HdesTypesMapper defs;
  private final HdesAstTypes ast;
  
  private Integer version;
  private final List<AstCommand> commands = new ArrayList<>();
  private ArrayNode json;
  
  public HdesClientAstBuilder(HdesTypesMapper defs, HdesAstTypes ast) {
    super();
    this.defs = defs;
    this.ast = ast;
  }
  
  @Override
  public AstService service() {
    return ast.service().src(json).src(commands).rev(version).build();
  }
  @Override
  public AstFlow flow() {
    return ast.flow().src(json).src(commands).rev(version).build();
  }
  @Override
  public AstDecision decision() {
    return ast.decision().src(json).src(commands).rev(version).build();
  }
  @Override
  public AstBuilder commands(String src) {
    this.json = defs.commandsJson(src);
    return this;
  }
  @Override
  public AstBuilder syntax(InputStream syntax) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(syntax));
      String line;
      int index = 0;
      while ((line = br.readLine()) != null) {
        commands.add(ImmutableAstCommand.builder()
            .id(String.valueOf(index++))
            .type(AstCommandValue.ADD)
            .value(line)
            .build());
      }
      return this;
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      try {
        syntax.close();
      } catch(IOException e) {}
    }
  }
  @Override
  public AstBuilder commands(List<AstCommand> src, Integer version) {
    this.commands.addAll(src);
    this.version = version;
    return this;
  }
  @Override
  public AstBuilder commands(ArrayNode src, Integer version) {
    this.json = src;
    this.version = version;
    return this;
  }
  @Override
  public AstBuilder commands(List<AstCommand> src) {
    return commands(src, null);
  }
  @Override
  public AstBuilder commands(ArrayNode src) {
    return commands(src, null);
  }
}
