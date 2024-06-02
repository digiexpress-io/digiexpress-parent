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

import io.resys.hdes.client.api.HdesClient.EnvirBuilder;
import io.resys.hdes.client.api.HdesClient.EnvirCommandFormatBuilder;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.ImmutableAstSource;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.envir.ProgramEnvirFactory;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.resys.hdes.client.spi.util.Sha2;

public class HdesClientEnvirBuilder implements EnvirBuilder {
  private final ProgramEnvirFactory factory;
  private final HdesTypesMapper defs;
  private ProgramEnvir envir;
  
  public HdesClientEnvirBuilder(ProgramEnvirFactory factory, HdesTypesMapper defs) {
    super();
    this.factory = factory;
    this.defs = defs;
  }
  @Override
  public EnvirCommandFormatBuilder addCommand() {
    final EnvirBuilder enviBuilder = this;
    return new EnvirCommandFormatBuilder() {
      private String id;
      private AstBodyType type;
      private String commandJson;
      private StoreEntity entity;
      private boolean cachless;
      
      @Override
      public EnvirCommandFormatBuilder id(String externalId) {
        this.id = externalId;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder service(String commandJson) {
        this.type = AstBodyType.FLOW_TASK;
        this.commandJson = commandJson;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder flow(String commandJson) {
        this.type = AstBodyType.FLOW;
        this.commandJson = commandJson;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder decision(String commandJson) {
        this.type = AstBodyType.DT;
        this.commandJson = commandJson;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder service(StoreEntity entity) {
        this.type = AstBodyType.FLOW_TASK;
        this.entity = entity;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder flow(StoreEntity entity) {
        this.type = AstBodyType.FLOW;
        this.entity = entity;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder decision(StoreEntity entity) {
        this.type = AstBodyType.DT;
        this.entity = entity;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder cachless() {
        this.cachless = true;
        return this;
      }
      @Override
      public EnvirBuilder build() {
        HdesAssert.notNull(id, () -> "id must be defined!");
        HdesAssert.isTrue(commandJson != null || entity != null, () -> "commandJson or entity must be defined!");
        HdesAssert.isTrue(commandJson == null || entity == null, () -> "commandJson and entity can't be both defined!");
        
        factory.add(ImmutableAstSource.builder()
            .id(id)
            .bodyType(type)
            .hash(entity == null ? Sha2.blob(commandJson) : entity.getHash())
            .commands(entity == null ? defs.commandsList(commandJson) : entity.getBody())
            .build(), cachless);
        return enviBuilder;
      }
    };
  }
  @Override
  public EnvirBuilder from(ProgramEnvir envir) {
    this.envir = envir;
    return this;
  }
  @Override
  public ProgramEnvir build() {
    return factory.add(envir).build();
  }

}
