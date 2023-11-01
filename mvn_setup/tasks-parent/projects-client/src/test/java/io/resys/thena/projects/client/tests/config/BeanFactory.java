package io.resys.thena.projects.client.tests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.jackson.ObjectMapperCustomizer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.resys.thena.docdb.spi.jackson.VertexExtModule;
import io.resys.thena.projects.client.api.model.ImmutableCreateProject;
import io.resys.thena.projects.client.api.model.ImmutableProject;
import io.vertx.core.json.jackson.VertxModule;
/*-
 * #%L
 * thena-quarkus-dev-app
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
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

@Dependent
@RegisterForReflection(targets = {
    ImmutableProject.class,
    ImmutableCreateProject.class
})
public class BeanFactory {

  @Produces
  public ObjectMapperCustomizer objectMapperCustomizer() {
    final var modules = new com.fasterxml.jackson.databind.Module[] {
      new JavaTimeModule(), 
      new Jdk8Module(), 
      new GuavaModule(),
      new VertxModule(),
      new VertexExtModule()
    };
    
    return new ObjectMapperCustomizer() {
      public void customize(ObjectMapper mapper) {
        mapper.registerModules(modules);
        // without this, local dates will be serialized as int array
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      }
    };
  }

}
