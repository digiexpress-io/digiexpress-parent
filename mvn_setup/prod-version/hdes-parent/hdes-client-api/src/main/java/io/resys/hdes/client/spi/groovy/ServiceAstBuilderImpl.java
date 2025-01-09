package io.resys.hdes.client.spi.groovy;

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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import groovy.lang.GroovyClassLoader;
import io.resys.hdes.client.api.HdesAstTypes.DataTypeAstBuilder;
import io.resys.hdes.client.api.HdesAstTypes.ServiceAstBuilder;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstBody.CommandMessageType;
import io.resys.hdes.client.api.ast.AstBody.Headers;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.ast.AstService.AstServiceRef;
import io.resys.hdes.client.api.ast.AstService.AstServiceType;
import io.resys.hdes.client.api.ast.AstService.ServiceExecutorType;
import io.resys.hdes.client.api.ast.AstService.ServiceExecutorType0;
import io.resys.hdes.client.api.ast.AstService.ServiceExecutorType1;
import io.resys.hdes.client.api.ast.AstService.ServiceExecutorType2;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.ImmutableAstCommandMessage;
import io.resys.hdes.client.api.ast.ImmutableAstService;
import io.resys.hdes.client.api.ast.ImmutableAstServiceRef;
import io.resys.hdes.client.api.ast.ImmutableHeaders;
import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Direction;
import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.exceptions.ServiceAstException;
import io.resys.hdes.client.api.programs.ServiceData;
import io.resys.hdes.client.api.programs.ServiceData.ServiceRef;
import io.resys.hdes.client.spi.changeset.AstChangesetFactory;
import io.resys.hdes.client.spi.util.HdesAssert;


public class ServiceAstBuilderImpl implements ServiceAstBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAstBuilderImpl.class);
  private final HdesTypesMapper dataTypeRepository;
  private final List<AstCommand> src = new ArrayList<>();
  private Integer rev;
  private final GroovyClassLoader gcl;
  
  @Value.Immutable
  interface ServiceDataTypes {
    Headers getHeaders();
    @Nullable
    TypeDef getAcceptType0();
    @Nullable
    TypeDef getAcceptType1();
    @Nullable
    TypeDef getReturnType();
  }
  
  
  public static class FailSafeService implements ServiceExecutorType0<HashMap<String, String>> {
    @Override
    public HashMap<String, String> execute() {
      return new HashMap<>();
    }
  }
  

  public ServiceAstBuilderImpl(HdesTypesMapper dataTypeRepository, GroovyClassLoader gcl) {
    super();
    this.dataTypeRepository = dataTypeRepository;
    this.gcl = gcl;
  }

  @Override
  public ServiceAstBuilderImpl src(ArrayNode src) {
    if (src == null) {
      return this;
    }
    for (JsonNode node : src) {
      final String type = getString(node, "type");
      this.src.add(ImmutableAstCommand.builder().id(getString(node, "id")).value(getString(node, "value"))
          .type(AstCommandValue.valueOf(type)).build());
    }
    return this;
  }

  @Override
  public ServiceAstBuilderImpl src(List<AstCommand> src) {
    if (src == null) {
      return this;
    }
    this.src.addAll(src);
    return this;
  }

  @Override
  public ServiceAstBuilderImpl rev(Integer rev) {
    this.rev = rev;
    return this;
  }

  @Override
  public AstService build() {
    HdesAssert.notNull(src, () -> "src can't ne null!");

    final var value = new StringBuilder();
    final var changes = AstChangesetFactory.src(src, rev);
    final var iterator = changes.getSrc().iterator();

    while (iterator.hasNext()) {
      final var src = iterator.next();
      String lineContent = src.getCommands().get(src.getCommands().size() - 1).getValue();

      if (!StringUtils.isEmpty(lineContent)) {
        value.append(lineContent);
      }
      value.append(System.lineSeparator());
    }
    final var source = buildSource(value);
    
    try {
      @SuppressWarnings("unchecked")
      final Class<ServiceExecutorType> beanType = gcl.parseClass(source);
      final AstServiceType executorType;
      if(ServiceExecutorType0.class.isAssignableFrom(beanType)) {
        executorType = AstServiceType.TYPE_0;
      } else if(ServiceExecutorType1.class.isAssignableFrom(beanType)) {
        executorType = AstServiceType.TYPE_1;
      } else if(ServiceExecutorType2.class.isAssignableFrom(beanType)) {
        executorType = AstServiceType.TYPE_2;
      } else {
        throw new ServiceAstException(
            System.lineSeparator() +
            "Failed to generate groovy service ast because service executor type could not be determined for: " + System.lineSeparator() +
            source + System.lineSeparator()); 
      }
      
      final ServiceDataTypes method = getHeaders(beanType);

      
      return ImmutableAstService.builder()
          .bodyType(AstBodyType.FLOW_TASK)
          .name(beanType.getSimpleName())
          .headers(method.getHeaders())
          .typeDef0(method.getAcceptType0())
          .typeDef1(method.getAcceptType1())
          .returnDef1(method.getReturnType())
          .beanType(beanType)
          .executorType(executorType)
          .value(source)
          .refs(getRefs(beanType))
          .build();
    } catch (Exception e) {
      final var msg = "Failed to generate groovy service ast from: " + System.lineSeparator() + 
          source + System.lineSeparator() + e.getMessage();
      LOGGER.error(msg, e);
      
      return ImmutableAstService.builder()
          .bodyType(AstBodyType.FLOW_TASK)
          .name(parseFailSafeName(source))
          .headers(ImmutableHeaders.builder().build())
          .beanType(FailSafeService.class)
          .executorType(AstServiceType.TYPE_0)
          .addMessages(ImmutableAstCommandMessage.builder()
            .line(0)
            .value("message: " + e.getMessage())
            .type(CommandMessageType.ERROR)
            .build())
          .value(source).build();
    }
  }

  private String parseFailSafeName(String source) {
    try {
      final var def = "public class "; 
      final var clean = source.replaceAll("  ", " ");
      final var defIndex = clean.indexOf(def);
      if(defIndex < 0) {
        return UUID.randomUUID().toString();    
      }
      final var start = defIndex + def.length();
      final var end = clean.indexOf(" ", start);
      return clean.substring(start, end);
    } catch(Exception e) {
      return UUID.randomUUID().toString();  
    }
  }
  
  public List<AstServiceRef> getRefs(Class<ServiceExecutorType> beanType) {
    final List<AstServiceRef> result = new ArrayList<>();
    final Set<String> usedRefs = new HashSet<>();
    for(ServiceRef ref : beanType.getDeclaredAnnotationsByType(ServiceRef.class)) {
      
      if(usedRefs.contains(ref.value())) {
        continue;
      }
      
      usedRefs.add(ref.value());
      result.add(ImmutableAstServiceRef.builder()
          .bodyType(ref.type())
          .refValue(ref.value())
          .build());
    }
    
    return result;
  }
  
  protected ServiceDataTypes getHeaders(Class<?> beanType) {
    List<ServiceDataTypes> result = new ArrayList<>();
    for (Method method : beanType.getDeclaredMethods()) {
      if (method.getName().equals("execute") && Modifier.isPublic(method.getModifiers())
          && !Modifier.isVolatile(method.getModifiers())) {

        ServiceDataTypes params = getParams(method);
        HdesAssert.isTrue(result.isEmpty(), () -> "Only one 'execute' method allowed!");
        result.add(params);
      }
    }
    HdesAssert.isTrue(result.size() == 1, () -> "There must be one 'execute' method!");
    return result.iterator().next();
  }

  private ServiceDataTypes getParams(Method method) {
    TypeDef acceptType0 = null;
    TypeDef acceptType1 = null;
    final var result = ImmutableHeaders.builder();
    int index = 0;
    for (Parameter parameter : method.getParameters()) {
      Class<?> type = parameter.getType();
      boolean isData = type.isAnnotationPresent(ServiceData.class);
      if(isData) {
        
        DataTypeAstBuilder dataTypeBuilder = dataTypeRepository.dataType().id("input-" + index).order(index++)
            .data(isData).name(parameter.getName()).direction(Direction.IN).beanType(parameter.getType())
            .valueType(ValueType.OBJECT);
        getWrenchFlowParameter(dataTypeBuilder, parameter.getType(), isData, Direction.IN);
        if(acceptType0 == null) {
          acceptType0 = dataTypeBuilder.build();
        } else {
          acceptType1 = dataTypeBuilder.build();  
        }

        result.addAllAcceptDefs(getFields(parameter.getType(), Direction.IN));
      } else {
        DataTypeAstBuilder dataTypeBuilder = dataTypeRepository.dataType().id("input-" + index).order(index++)
            .data(isData).name(parameter.getName()).direction(Direction.IN).beanType(parameter.getType())
            .valueType(ValueType.OBJECT);
        
        if(acceptType0 == null) {
          acceptType0 = dataTypeBuilder.build();
        } else {
          acceptType1 = dataTypeBuilder.build();  
        }
      }
    }

    TypeDef returnTypeDef = null;
    Class<?> returnType = method.getReturnType();
    if (!returnType.isAnnotationPresent(ServiceData.class)) {
      throw new ServiceAstException(
          "'execute' must be void or return type must define: " + ServiceData.class.getCanonicalName() + "!");
    } else {
      DataTypeAstBuilder dataTypeBuilder = dataTypeRepository.dataType().id("output").name(returnType.getSimpleName())
          .data(true).order(index++).direction(Direction.OUT).beanType(returnType).valueType(ValueType.OBJECT);
      getWrenchFlowParameter(dataTypeBuilder, returnType, true, Direction.OUT);
      returnTypeDef = dataTypeBuilder.build();
      
      result.addAllReturnDefs(getFields(returnType, Direction.OUT));
    }

    return ImmutableServiceDataTypes.builder()
        .headers(result.build())
        .acceptType0(acceptType0)
        .acceptType1(acceptType1)
        .returnType(returnTypeDef)
        .build();
  }

  
  private List<TypeDef> getFields(Class<?> type, Direction direction) {
    List<TypeDef> result = new ArrayList<>();
    int index = 0;

    HdesAssert.isTrue(Serializable.class.isAssignableFrom(type), () -> "Flow types must implement Serializable!");
    for (Field field : type.getDeclaredFields()) {
      int modifier = field.getModifiers();
      if (Modifier.isFinal(modifier) || Modifier.isTransient(modifier) || Modifier.isStatic(modifier)
          || field.getName().startsWith("$") || field.getName().startsWith("_")) {
        continue;
      }
      final var typeDef = dataTypeRepository.dataType().id(field.getName()).order(index++).name(field.getName()).direction(direction).beanType(field.getType()).build();
      result.add(typeDef);
    }
    return result;
  }
  
  private void getWrenchFlowParameter(DataTypeAstBuilder parentDataTypeBuilder, Class<?> type, boolean isServiceData,
      Direction direction) {
    if (!isServiceData) {
      return;
    }
    int index = 0;

    HdesAssert.isTrue(Serializable.class.isAssignableFrom(type), () -> "Flow types must implement Serializable!");
    for (Field field : type.getDeclaredFields()) {
      int modifier = field.getModifiers();
      if (Modifier.isFinal(modifier) || Modifier.isTransient(modifier) || Modifier.isStatic(modifier)
          || field.getName().startsWith("$") || field.getName().startsWith("_")) {
        continue;
      }
      parentDataTypeBuilder.property().id(field.getName()).order(index++).name(field.getName()).direction(direction)
          .beanType(field.getType()).build();
    }
  }

  private String buildSource(StringBuilder value) {
    String result = value.toString();
    if (result.endsWith(System.lineSeparator())) {
      return result.substring(0, result.length() - System.lineSeparator().length());
    }
    return result;
  }

  protected String getString(JsonNode node, String name) {
    return node.hasNonNull(name) ? node.get(name).asText() : null;
  }
}
