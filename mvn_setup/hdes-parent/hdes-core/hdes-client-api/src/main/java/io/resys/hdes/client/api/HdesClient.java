package io.resys.hdes.client.api;

import java.io.InputStream;

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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.resys.hdes.client.api.HdesAstTypes.DataTypeAstBuilder;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionResult;
import io.resys.hdes.client.api.programs.ExpressionProgram;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResultLog;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ServiceProgram;
import io.resys.hdes.client.api.programs.ServiceProgram.ServiceResult;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.smallrye.mutiny.Uni;

public interface HdesClient {
  AstBuilder ast();
  ProgramBuilder program();
  ExecutorBuilder executor(ProgramEnvir envir);
  EnvirBuilder envir();
  
  HdesTypesMapper mapper();
  HdesAstTypes types();
  HdesStore store();
  CSVBuilder csv();
  ClientRepoBuilder repo();
  HdesClientConfig config();
  
  interface ClientRepoBuilder {
    ClientRepoBuilder repoName(String repoName);
    ClientRepoBuilder headName(String headName);
    Uni<HdesClient> create();
    HdesClient build();
  }
  
  interface HdesTypesMapper {
    DataTypeAstBuilder dataType();
    ExpressionProgram expression(ValueType valueType, String src);
    String commandsString(List<AstCommand> commands);
    ArrayNode commandsJson(String commands);
    List<AstCommand> commandsList(String commands);
    Map<String, Serializable> toMap(Object entity);
    Map<String, Serializable> toMap(JsonNode entity);
    Object toType(Object value, Class<?> toType);
    String toJson(Object anyObject);
  }
  
  
  interface EnvirBuilder {
    EnvirBuilder from(ProgramEnvir envir);
    EnvirCommandFormatBuilder addCommand();
    ProgramEnvir build();
  }

  
  interface EnvirCommandFormatBuilder {
    EnvirCommandFormatBuilder id(String externalId);
    EnvirCommandFormatBuilder cachless();
    
    EnvirCommandFormatBuilder tag(String commandJson);
    EnvirCommandFormatBuilder flow(String commandJson);
    EnvirCommandFormatBuilder decision(String commandJson);
    EnvirCommandFormatBuilder service(String commandJson);
    
    EnvirCommandFormatBuilder tag(StoreEntity entity);
    EnvirCommandFormatBuilder flow(StoreEntity entity);
    EnvirCommandFormatBuilder decision(StoreEntity entity);
    EnvirCommandFormatBuilder service(StoreEntity entity);
    
    EnvirBuilder build();
  }
  
  interface CSVBuilder {
    String ast(AstDecision ast);
  }

  interface ProgramBuilder {
    FlowProgram ast(AstFlow ast);
    DecisionProgram ast(AstDecision ast);
    ServiceProgram ast(AstService ast);
  }
  
  interface ProgramExecutor {}
  
  interface FlowExecutor extends ProgramExecutor {
    @Nullable
    FlowResultLog andGetTask(String task);
    FlowResult andGetBody();
  }
  interface DecisionExecutor extends ProgramExecutor {
    Map<String, Serializable> andGet();
    List<Map<String, Serializable>> andFind();
    DecisionResult andGetBody();
  }
  interface ServiceExecutor extends ProgramExecutor {
    ServiceResult andGetBody();
  }
  
  interface ExecutorInput extends Function<TypeDef, Object> {};
  
  interface ExecutorBuilder {
    ExecutorBuilder inputField(String name, Serializable value);
    ExecutorBuilder inputMap(Map<String, Serializable> input);
    ExecutorBuilder inputEntity(Object inputObject);
    ExecutorBuilder inputList(List<Object> inputObject);
    ExecutorBuilder inputJson(JsonNode json);
    ExecutorBuilder input(ExecutorInput input);
    
    FlowExecutor flow(String nameOrId);
    DecisionExecutor decision(String nameOrId);
    ServiceExecutor service(String nameOrId);
  }
  
  interface AstBuilder {
    AstBuilder commands(ArrayNode src, Integer version);
    AstBuilder commands(ArrayNode src);
    AstBuilder commands(String src);
    AstBuilder commands(List<AstCommand> src, Integer version);
    AstBuilder commands(List<AstCommand> src);
    AstBuilder syntax(InputStream syntax);


    AstFlow flow();
    AstDecision decision();
    AstService service();
    AstTag tag();
  }
}
