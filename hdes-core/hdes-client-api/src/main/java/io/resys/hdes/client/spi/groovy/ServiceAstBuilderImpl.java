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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import groovy.lang.GroovyClassLoader;
import io.resys.hdes.client.api.HdesAstTypes.DataTypeAstBuilder;
import io.resys.hdes.client.api.HdesAstTypes.ServiceAstBuilder;
import io.resys.hdes.client.api.ast.AstBody.Headers;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.ImmutableAstService;
import io.resys.hdes.client.api.ast.ImmutableHeaders;
import io.resys.hdes.client.api.ast.TypeDef.Direction;
import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.exceptions.ServiceAstException;
import io.resys.hdes.client.api.execution.ServiceData;
import io.resys.hdes.client.spi.HdesDataTypeFactory;
import io.resys.hdes.client.spi.changeset.AstChangesetFactory;
import io.resys.hdes.client.spi.util.Assert;

public class ServiceAstBuilderImpl implements ServiceAstBuilder {

  
  private final HdesDataTypeFactory dataTypeRepository;
  private final List<AstCommand> src = new ArrayList<>();
  private Integer rev;
  private final GroovyClassLoader gcl;

  public ServiceAstBuilderImpl(HdesDataTypeFactory dataTypeRepository, GroovyClassLoader gcl) {
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
    Assert.notNull(src, () -> "src can't ne null!");

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
      final Class<?> beanType = gcl.parseClass(source);
      final Headers method = getHeaders(beanType);
      
      return ImmutableAstService.builder()
          .name(beanType.getSimpleName())
          .headers(method)
          .src(source)
          .rev(changes.getCommands().size())
          .commands(changes.getCommands())
          .type(beanType)
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  protected Headers getHeaders(Class<?> beanType) {
    List<Headers> result = new ArrayList<>();
    for (Method method : beanType.getDeclaredMethods()) {
      if (method.getName().equals("execute") && Modifier.isPublic(method.getModifiers())
          && !Modifier.isVolatile(method.getModifiers())) {

        Headers params = getParams(method);
        Assert.isTrue(result.isEmpty(), () -> "Only one 'execute' method allowed!");
        result.add(params);
      }
    }
    Assert.isTrue(result.size() == 1, () -> "There must be one 'execute' method!");
    return result.iterator().next();
  }
  

  protected Headers getParams(Method method) {
    final var result = ImmutableHeaders.builder();
    int index = 0;
    for(Parameter parameter : method.getParameters()) {
      Class<?> type = parameter.getType();
      boolean isData = type.isAnnotationPresent(ServiceData.class);
      
      DataTypeAstBuilder dataTypeBuilder = dataTypeRepository.dataType().
          id("intput-" + index).
          order(index++).
          data(isData).
          name(parameter.getName()).
          direction(Direction.IN).
          beanType(parameter.getType()).
          valueType(ValueType.OBJECT);
      getWrenchFlowParameter(dataTypeBuilder, parameter.getType(), isData, Direction.IN);
      result.addAcceptDefs(dataTypeBuilder.build());
    }

    index = 0;
    Class<?> returnType = method.getReturnType();
    if(!returnType.isAnnotationPresent(ServiceData.class)) {
      throw new ServiceAstException("'execute' must be void or return type must define: " + ServiceData.class.getCanonicalName() + "!");
    } else {
      DataTypeAstBuilder dataTypeBuilder = dataTypeRepository.dataType().
          id("output").
          name(returnType.getSimpleName()).
          data(true).
          order(index++).
          direction(Direction.OUT).
          beanType(returnType).
          valueType(ValueType.OBJECT);
      getWrenchFlowParameter(dataTypeBuilder, returnType, true, Direction.OUT);
      result.addReturnDefs(dataTypeBuilder.build());
    }

    return result.build();
  }


  protected void getWrenchFlowParameter(DataTypeAstBuilder parentDataTypeBuilder, Class<?> type, boolean isServiceData, Direction direction) {
    if(!isServiceData) {
      return;
    }
    int index = 0;

    Assert.isTrue(Serializable.class.isAssignableFrom(type), () -> "Flow types must implement Serializable!");
    for(Field field : type.getDeclaredFields()) {
      int modifier = field.getModifiers();
      if( Modifier.isFinal(modifier) ||
          Modifier.isTransient(modifier) ||
          Modifier.isStatic(modifier) ||
          field.getName().startsWith("$") ||
          field.getName().startsWith("_")) {
        continue;
      }
      parentDataTypeBuilder.property()
        .id(field.getName())
        .order(index++)
        .name(field.getName()).direction(direction).beanType(field.getType()).build();
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
