package io.resys.hdes.client.spi.tag;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import io.resys.hdes.client.api.HdesAstTypes.TagAstBuilder;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTag.AstTagValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.ImmutableAstTag;
import io.resys.hdes.client.api.ast.ImmutableAstTagValue;
import io.resys.hdes.client.api.ast.ImmutableHeaders;
import io.resys.hdes.client.spi.staticresources.Sha2;

public class TagAstBuilderImpl implements TagAstBuilder {
  private final HdesTypesMapper typeDefs;
  private final List<AstCommand> src = new ArrayList<>();
  public TagAstBuilderImpl(HdesTypesMapper typeDefs) {
    super();
    this.typeDefs = typeDefs;
  }

  @Override
  public TagAstBuilder src(List<AstCommand> src) {
    if(src == null) {
      return this;
    }
    this.src.addAll(src);
    return this;
  }
  @Override
  public TagAstBuilder src(JsonNode src) {
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
  public AstTag build() {
    String name = null;
    String desc = null;
    LocalDateTime created = null;
    final List<AstTagValue> values = new ArrayList<>();
    
    for(AstCommand v : src) {
      final var id = v.getId() == null ? UUID.randomUUID().toString() : v.getId();
      
      switch (v.getType()) {
      case SET_TAG_NAME: name = v.getValue(); break;
      case SET_TAG_DESC: desc = v.getValue(); break;
      case SET_TAG_CREATED: created = LocalDateTime.parse(v.getValue()); break;
      case SET_TAG_DT: {
        values.add(ImmutableAstTagValue.builder()
            .id(id)
            .hash(Sha2.blob(v.getValue()))
            .bodyType(AstBodyType.DT).commands(typeDefs.commandsList(v.getValue()))
            .build());
        break;
      }
      case SET_TAG_FL: {
        values.add(ImmutableAstTagValue.builder()
            .id(id)
            .hash(Sha2.blob(v.getValue()))
            .bodyType(AstBodyType.FLOW)
            .addCommands(ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(v.getValue()).build())
            .build());
        break;
      }
      case SET_TAG_ST: {
        values.add(ImmutableAstTagValue.builder()
            .id(id)
            .hash(Sha2.blob(v.getValue()))
            .bodyType(AstBodyType.FLOW_TASK)
            .addCommands(ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(v.getValue()).build())
            .build());
        break;
      }
      default:
        break;
      } 
    }

    return ImmutableAstTag.builder()
        .headers(ImmutableHeaders.builder().build())
        .bodyType(AstBodyType.TAG)
        .name(name)
        .description(desc)
        .created(created)
        .values(values)
        .build();
  }

  protected String getString(JsonNode node, String name) {
    return node.hasNonNull(name) ? node.get(name).asText() : null;
  }
}
