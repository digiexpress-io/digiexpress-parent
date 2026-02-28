package io.resys.limaone.spi.ast;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

import groovy.lang.GroovyClassLoader;
import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.AST_Parser.FlowTaskParser;
import io.resys.limaone.ast.FlowTask_AST;
import io.resys.limaone.ast.FlowTask_AST.ServiceRef;
import io.resys.limaone.ast.ImmutableFlowTask_AST;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.ast.ImmutableMessage_AST;
import io.resys.limaone.ast.ImmutableServiceRef;
import io.resys.limaone.ast.Simple_AST.MessageType;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.FlowTask.FlowTaskExecutable;
import io.resys.limaone.model.FlowTask.FlowTaskPropType;
import io.resys.limaone.model.FlowTask.ServiceData;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType0;
import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType1;
import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType2;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.FlowTask_AST_CacheKey;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.flowtask.FailSafeService;
import io.resys.limaone.spi.ast.flowtask.ImmutableServiceDataTypes;
import io.resys.limaone.spi.ast.flowtask.ServiceDataTypes;
import io.resys.limaone.spi.parameter.Parameter_Factory;
import io.resys.thena.support.RepoAssert;



public class FlowTaskParserImpl implements AST_Parser.FlowTaskParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowTaskParserImpl.class);
  private final List<String> src = new ArrayList<>();
  private final GroovyClassLoader gcl;
  private String id;
  
  public FlowTaskParserImpl(AST_ParserProps props) {
    super();
    this.gcl = props.getGroovy();
  }

  @Override
  public FlowTaskParserImpl syntax(String src) {
    if (src == null) {
      return this;
    }
    this.src.add(src);
    return this;
  }

  @Override
  public FlowTaskParser id(String id) {
    this.id = id;
    return this;
  }

  @Override
  public FlowTask_AST parse() {
    Objects.requireNonNull(this.src, () -> "src can't ne null!");
    Objects.requireNonNull(this.id, () -> "id can't ne null!");
    
    final var source = String.join(System.lineSeparator(), this.src);
    
    
    final var hash = Hashing.murmur3_128().hashString(source, StandardCharsets.UTF_8).toString();
    final var cacheKey = new FlowTask_AST_CacheKey(hash);
    final Function<FlowTask_AST_CacheKey, FlowTask_AST> mappingFunction = (k) -> {
      try {
        @SuppressWarnings("unchecked")
        final Class<FlowTaskExecutable> beanType = gcl.parseClass(source);
        final FlowTaskPropType executorType;
        if(ServiceExecutorType0.class.isAssignableFrom(beanType)) {
          executorType = FlowTaskPropType.TYPE_0;
        } else if(ServiceExecutorType1.class.isAssignableFrom(beanType)) {
          executorType = FlowTaskPropType.TYPE_1;
        } else if(ServiceExecutorType2.class.isAssignableFrom(beanType)) {
          executorType = FlowTaskPropType.TYPE_2;
        } else {
          throw new AST_Exception(
              System.lineSeparator() +
              "Failed to generate groovy service ast because service executor type could not be determined for: " + System.lineSeparator() +
              source + System.lineSeparator()); 
        }
        
        final ServiceDataTypes method = getHeaders(beanType);

        
        return ImmutableFlowTask_AST.builder()
            .bodyType(Model.BodyType.FLOW_TASK)
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
        
        return ImmutableFlowTask_AST.builder()
            .id(this.id)
            .bodyType(Model.BodyType.FLOW_TASK)
            .name(parseFailSafeName(source))
            .headers(ImmutableHeaders_AST.builder().build())
            .beanType(FailSafeService.class)
            .executorType(FlowTaskPropType.TYPE_0)
            .addMessages(ImmutableMessage_AST.builder()
              .line(0)
              .value("message: " + e.getMessage())
              .type(MessageType.ERROR)
              .build())
            .value(source).build();
      }
    };
    return LocalCache.computeIfAbsent(cacheKey, mappingFunction);
    
    
    
    
    
    
  
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
  
  public List<ServiceRef> getRefs(Class<FlowTaskExecutable> beanType) {
    final List<ServiceRef> result = new ArrayList<>();
    final Set<String> usedRefs = new HashSet<>();
    for(FlowTask.ServiceRef ref : beanType.getDeclaredAnnotationsByType(FlowTask.ServiceRef.class)) {
      
      if(usedRefs.contains(ref.value())) {
        continue;
      }
      
      usedRefs.add(ref.value());
      result.add(ImmutableServiceRef.builder()
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
        RepoAssert.isTrue(result.isEmpty(), () -> "Only one 'execute' method allowed!");
        result.add(params);
      }
    }
    RepoAssert.isTrue(result.size() == 1, () -> "There must be one 'execute' method!");
    return result.iterator().next();
  }

  private ServiceDataTypes getParams(Method method) {
    io.resys.limaone.model.Parameter acceptType0 = null;
    io.resys.limaone.model.Parameter acceptType1 = null;
    final var result = ImmutableHeaders_AST.builder();
    int index = 0;
    for (Parameter parameter : method.getParameters()) {
      Class<?> type = parameter.getType();
      boolean isData = type.isAnnotationPresent(ServiceData.class);
      if(isData) {
        
        final var dataTypeBuilder = Parameter_Factory.newParam().id("input-" + index).order(index++)
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
        final var dataTypeBuilder = Parameter_Factory.newParam().id("input-" + index).order(index++)
            .data(isData).name(parameter.getName()).direction(Direction.IN).beanType(parameter.getType())
            .valueType(ValueType.OBJECT);
        
        if(acceptType0 == null) {
          acceptType0 = dataTypeBuilder.build();
        } else {
          acceptType1 = dataTypeBuilder.build();  
        }
      }
    }

    io.resys.limaone.model.Parameter returnTypeDef = null;
    Class<?> returnType = method.getReturnType();
    if (!returnType.isAnnotationPresent(ServiceData.class)) {
      throw new AST_Exception(
          "'execute' must be void or return type must define: " + ServiceData.class.getCanonicalName() + "!");
    } else {
      final var dataTypeBuilder = Parameter_Factory.newParam().id("output").name(returnType.getSimpleName())
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

  
  private List<io.resys.limaone.model.Parameter> getFields(Class<?> type, Direction direction) {
    List<io.resys.limaone.model.Parameter> result = new ArrayList<>();
    int index = 0;

    RepoAssert.isTrue(Serializable.class.isAssignableFrom(type), () -> "Flow types must implement Serializable!");
    for (Field field : type.getDeclaredFields()) {
      int modifier = field.getModifiers();
      if (Modifier.isFinal(modifier) || Modifier.isTransient(modifier) || Modifier.isStatic(modifier)
          || field.getName().startsWith("$") || field.getName().startsWith("_")) {
        continue;
      }
      
      final var typeDef = Parameter_Factory.newParam()
          .id(field.getName())
          .order(index++)
          .name(field.getName())
          .direction(direction)
          .beanType(field.getType())
          .required(Arrays.asList(field.getAnnotations()).stream().filter(ann -> ann.annotationType().getSimpleName().equals("Nullable")).findFirst().isEmpty())
          .build();
      result.add(typeDef);
    }
    return result;
  }
  
  private void getWrenchFlowParameter(Parameter_Factory.NewAttribute attribute, Class<?> type, boolean isServiceData, Direction direction) {
    if (!isServiceData) {
      return;
    }
    int index = 0;

    RepoAssert.isTrue(Serializable.class.isAssignableFrom(type), () -> "Flow types must implement Serializable!");
    for (Field field : type.getDeclaredFields()) {
      int modifier = field.getModifiers();
      if (Modifier.isFinal(modifier) || Modifier.isTransient(modifier) || Modifier.isStatic(modifier)
          || field.getName().startsWith("$") || field.getName().startsWith("_")) {
        continue;
      }
      attribute
        .property()
        .id(field.getName())
        .order(index++)
        .name(field.getName())
        .direction(direction)
        .beanType(field.getType())
        .build();
    }
  }
}
