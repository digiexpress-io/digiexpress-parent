package io.resys.hdes.client.spi.composer;

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

import io.resys.hdes.client.api.HdesClient.EnvirBuilder;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ImmutableComposerEntity;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstBranch;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTag.AstTagValue;
import io.resys.hdes.client.api.ast.ImmutableAstTag;
import io.resys.hdes.client.api.ast.ImmutableAstTagValue;
import io.resys.hdes.client.api.ast.ImmutableHeaders;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramWrapper;

public class ComposerEntityMapper {

  public static EnvirBuilder toEnvir(EnvirBuilder envirBuilder, AstTag source) {
    for(final var tag : source.getValues()) {
      final var entity = ImmutableStoreEntity.builder()
          .body(tag.getCommands())
          .bodyType(tag.getBodyType())
          .id(tag.getId())
          .hash(tag.getHash())
          .build();
      
      switch (tag.getBodyType()) {
        case DT: { 
          envirBuilder.addCommand().id(tag.getId()).decision(entity).build();
          break;
        }
        case FLOW: {
          envirBuilder.addCommand().id(tag.getId()).flow(entity).build();
          break;
        }
        case FLOW_TASK: {
          envirBuilder.addCommand().id(tag.getId()).service(entity).build();     
          break;
        }
        
        default: continue;
      }
    }
    return envirBuilder;
  }
  
  public static EnvirBuilder toEnvir(EnvirBuilder envirBuilder, StoreState source) {
    source.getDecisions().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).decision(v).build());
    source.getServices().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).service(v).build());
    source.getFlows().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).flow(v).build());
    source.getTags().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).tag(v).build());
    source.getBranches().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).branch(v).build());
    return envirBuilder;
  }
  
  public static void toComposer(ImmutableComposerState.Builder builder, ProgramWrapper<?, ?> wrapper) {
    switch (wrapper.getSource().getBodyType()) {
    case DT:
      final var dt = ImmutableComposerEntity.<AstDecision>builder()
        .id(wrapper.getId())
        .ast((AstDecision) wrapper.getAst().orElse(null))
        .status(wrapper.getStatus())
        .errors(wrapper.getErrors())
        .warnings(wrapper.getWarnings())
        .associations(wrapper.getAssociations())
        .source(wrapper.getSource())
        .build();
      builder.putDecisions(dt.getId(), dt);
      break;
    case FLOW:
      final var flow = ImmutableComposerEntity.<AstFlow>builder()
        .id(wrapper.getId())
        .ast((AstFlow) wrapper.getAst().orElse(null))
        .status(wrapper.getStatus())
        .errors(wrapper.getErrors())
        .warnings(wrapper.getWarnings())
        .associations(wrapper.getAssociations())
        .source(wrapper.getSource())
        .build();
      builder.putFlows(flow.getId(), flow);
      break;
    case FLOW_TASK:
      final var service = ImmutableComposerEntity.<AstService>builder()
        .id(wrapper.getId())
        .ast((AstService) wrapper.getAst().orElse(null))
        .status(wrapper.getStatus())
        .errors(wrapper.getErrors())
        .warnings(wrapper.getWarnings())
        .associations(wrapper.getAssociations())
        .source(wrapper.getSource())
        .build();
      builder.putServices(service.getId(), service);
      break;
    case TAG:
      final var tag = ImmutableComposerEntity.<AstTag>builder()
        .id(wrapper.getId())
        .ast((AstTag) wrapper.getAst().orElse(null))
        .status(wrapper.getStatus())
        .errors(wrapper.getErrors())
        .warnings(wrapper.getWarnings())
        .associations(wrapper.getAssociations())
        .source(wrapper.getSource())
        .build();
      builder.putTags(tag.getId(), tag);
      break;
    case BRANCH:
      final var branch = ImmutableComposerEntity.<AstBranch>builder()
        .id(wrapper.getId())
        .ast((AstBranch) wrapper.getAst().orElse(null))
        .status(wrapper.getStatus())
        .errors(wrapper.getErrors())
        .warnings(wrapper.getWarnings())
        .associations(wrapper.getAssociations())
        .source(wrapper.getSource())
        .build();
      builder.putBranches(branch.getId(), branch);
      break;
    default:
      break;
    }
  }
  
  
  
  public static AstTag toTag(StoreState source) {
    final var builder = ImmutableAstTag.builder();
    
    source.getDecisions().values().forEach(v -> builder.addValues(toTagValue(v)));
    source.getServices().values().forEach(v -> builder.addValues(toTagValue(v)));
    source.getFlows().values().forEach(v -> builder.addValues(toTagValue(v)));
    
    return builder
        .name(source.getTagName())
        .created(source.getCommitAt())
        .commitId(source.getCommitId())
        .headers(ImmutableHeaders.builder().build())
        .bodyType(AstBodyType.TAG)
        .build();
  }
  
  private static AstTagValue toTagValue(StoreEntity entity) {
    final var tagValue = ImmutableAstTagValue
      .builder()
      .id(entity.getId())
      .hash(entity.getHash())
      .commands(entity.getBody())
      .bodyType(entity.getBodyType())
      .build();
    
    return tagValue;
  }
}
