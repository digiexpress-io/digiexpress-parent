package io.resys.hdes.client.spi.branch;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import com.fasterxml.jackson.databind.JsonNode;
import io.resys.hdes.client.api.HdesAstTypes;
import io.resys.hdes.client.api.HdesAstTypes.BranchAstBuilder;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.api.ast.AstBranch;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstBranch;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.ImmutableHeaders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BranchAstBuilderImpl implements HdesAstTypes.BranchAstBuilder {
  private final HdesTypesMapper typeDefs;
  private final List<AstCommand> src = new ArrayList<>();
  public BranchAstBuilderImpl(HdesTypesMapper typeDefs) {
    super();
    this.typeDefs = typeDefs;
  }

  @Override
  public BranchAstBuilder src(List<AstCommand> src) {
    if(src == null) {
      return this;
    }
    this.src.addAll(src);
    return this;
  }
  @Override
  public BranchAstBuilder src(JsonNode src) {
    if(src == null) {
      return this;
    }
    for(JsonNode node : src) {
      final String type = getString(node, "type");
      this.src.add(ImmutableAstCommand.builder().id(getString(node, "id")).value(getString(node, "value")).type(AstCommandValue.valueOf(type)).build());
    }
    return this;
  }

  @Override
  public AstBranch build() {
    String name = null;
    LocalDateTime created = null;
    String tagId = null;
    
    for(AstCommand v : src) {
      switch (v.getType()) {
      case SET_BRANCH_NAME: name = v.getValue(); break;
      case SET_BRANCH_CREATED: created = LocalDateTime.parse(v.getValue()); break;
      case SET_BRANCH_TAG: tagId = v.getValue(); break;
      default:
        break;
      } 
    }

    return ImmutableAstBranch.builder()
        .name(name)
        .created(created)
        .tagId(tagId)
        .bodyType(AstBody.AstBodyType.BRANCH)
        .headers(ImmutableHeaders.builder().build())
        .build();
  }

  protected String getString(JsonNode node, String name) {
    return node.hasNonNull(name) ? node.get(name).asText() : null;
  }
}
