package io.resys.hdes.client.api.programs;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.TypeDef;

@Value.Immutable
public interface ProgramEnvir {  
  Map<String, ProgramWrapper<?, ?>> getValues();

  @Value.Immutable
  interface ProgramWrapper<A extends AstBody, P extends Program<?>> {
    String getId();
    AstBodyType getType();
    ProgramStatus getStatus();
    
    List<ProgramMessage> getWarnings();
    List<ProgramMessage> getErrors();
    List<TypeDef> getHeaders();
    List<ProgramAssociation> getAssociations();
    
    @JsonIgnore
    Optional<A> getAst();
    @JsonIgnore
    Optional<P> getProgram();
  }
  
  @Value.Immutable
  interface ProgramAssociation {
    Optional<String> getId();
    String getRef();
    AstBodyType getRefType();
    ProgramStatus getRefStatus();
    Boolean getOwner();
  }

  @Value.Immutable
  interface ProgramMessage {
    String getId();
    String getMsg();
    @JsonIgnore
    @Nullable
    Exception getException();
  }

  enum ProgramStatus { UP, 
    AST_ERROR, 
    PROGRAM_ERROR, 
    DEPENDENCY_ERROR }
}
