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
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ImmutableComposerEntity;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramWrapper;

public class ComposerEntityMapper {

  
  public static EnvirBuilder toEnvir(EnvirBuilder envirBuilder, StoreState source) {
    source.getDecisions().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).decision(v).build());
    source.getServices().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).service(v).build());
    source.getFlows().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).flow(v).build());
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
    
    default:
      break;
    }
  }
}
