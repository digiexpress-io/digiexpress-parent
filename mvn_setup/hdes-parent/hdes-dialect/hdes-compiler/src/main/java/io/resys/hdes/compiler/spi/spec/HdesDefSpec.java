package io.resys.hdes.compiler.spi.spec;

/*-
 * #%L
 * hdes-compiler
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.compiler.api.HdesCompiler.ResourceType;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerType;
import io.resys.hdes.executor.api.HdesDefContinue;
import io.resys.hdes.executor.api.HdesDefContinue.HdesWakeup;

public class HdesDefSpec {
  public static final String METHOD_APPLY = "apply"; 
  public final static String ACCESS_INPUT_VALUE = "input";
  public final static String ACCESS_WAKEUP_VALUE = "wakeup";
  public final static String ACCESS_WAKEUP_TRACE = "trace";
  
  public static ApiBuilder api(CompilerType compilerType) {
    return new ApiBuilder(compilerType);
  }

  public static ImplBuilder impl(CompilerType compilerType) {
    Assertions.notNull(compilerType, () -> "compilerType must be defined!");
    return new ImplBuilder(compilerType);
  }
  
  public static class ImplBuilder {
    private final CompilerType compilerType;
    private final List<MethodSpec> methods = new ArrayList<>();
    private final List<FieldSpec> fields = new ArrayList<>();
    private CodeBlock execution;
    private CodeBlock wakeup;

    private ImplBuilder(CompilerType compilerType) {
      super();
      this.compilerType = compilerType;
    }
    
    public ImplBuilder execution(CodeBlock execution) {
      this.execution = execution;
      return this;
    }
    public ImplBuilder wakeup(CodeBlock wakeup) {
      if(!wakeup.isEmpty()) {
        this.wakeup = wakeup;
      }
      return this;
    }
    
    public ImplBuilder method(MethodSpec method) {
      methods.add(method);
      return this;
    }
    
    public ImplBuilder field(FieldSpec field) {
      fields.add(field);
      return this;
    }
    
    public ImplBuilder apply(Consumer<ImplBuilder> consumer) {
      consumer.accept(this);
      return this;
    }
    
    public TypeSpec.Builder build() {
      Assertions.notNull(execution, () -> "execution must be defined!");
      
      List<AnnotationSpec> annotations = Arrays.asList(
          AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build(),
          AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", HdesDefSpec.class.getCanonicalName()).build());
      
      MethodSpec apply = MethodSpec.methodBuilder(HdesDefSpec.METHOD_APPLY)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), ACCESS_INPUT_VALUE).build())
        .addCode(execution)
        .returns(compilerType.getReturnType().getName())
        .build();
      
      
      final var result = TypeSpec.classBuilder(compilerType.getImpl().getName())
          .addModifiers(Modifier.PUBLIC)
          .addAnnotations(annotations)
          .addSuperinterface(compilerType.getImpl().getSuperinterface())
          .addFields(fields)
          .addMethod(apply)
          .addMethods(this.methods);
      
      if(this.wakeup != null) {
        result.addSuperinterface(
            ParameterizedTypeName.get(
                ClassName.get(HdesDefContinue.class), 
                compilerType.getAccepts().getName(), compilerType.getReturnType().getName()));
        
        MethodSpec wakeup = MethodSpec.methodBuilder(HdesDefSpec.METHOD_APPLY)
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(compilerType.getReturnType().getName(), ACCESS_WAKEUP_TRACE).build())
            .addParameter(ParameterSpec.builder(HdesWakeup.class, ACCESS_WAKEUP_VALUE).build())
            .addCode(this.wakeup)
            .returns(compilerType.getReturnType().getName())
            .build();
        result.addMethod(wakeup);
      }
      
      return result;
    }
  }
  
  public static class ApiBuilder {
    private final CompilerType compilerType;
    private final List<TypeSpec> types = new ArrayList<>();

    private ApiBuilder(CompilerType compilerType) {
      super();
      this.compilerType = compilerType;
      this.immutable(compilerType.getReturnType().getName())
        .superinterface(compilerType.getReturnType().getSuperinterface())
        .method("body").isNullable(compilerType.getSourceType() == ResourceType.FL).returns(compilerType.getReturns().getName()).build()
        .build();
    }

    public ApiBuilder apply(Consumer<ApiBuilder> consumer) {
      consumer.accept(this);
      return this;
    }
    
    public ImmutableSpec.ImmutableBuilder immutable(ClassName name) {
      return ImmutableSpec.builder(name).callback(t -> types.add(t));
    }
    
    public ImmutableSpec.ImmutableBuilder inputValue() {
      return ImmutableSpec.inputValue(compilerType).callback(t -> types.add(t));
    }
    public ImmutableSpec.ImmutableBuilder inputValue(ClassName name) {
      return ImmutableSpec.inputValue(name).callback(t -> types.add(t));
    }
    
    public ImmutableSpec.ImmutableBuilder outputValue() {
      return ImmutableSpec.outputValue(compilerType).callback(t -> types.add(t));
    }
    public ImmutableSpec.ImmutableBuilder outputValue(ClassName name) {
      return ImmutableSpec.outputValue(name).callback(t -> types.add(t));
    }
    
    public TypeSpec.Builder build() {
      final AnnotationSpec annotation = AnnotationSpec.builder(javax.annotation.processing.Generated.class)
          .addMember("value", "$S", HdesDefSpec.class.getCanonicalName()).build();

      MethodSpec apply = MethodSpec.methodBuilder(HdesDefSpec.METHOD_APPLY)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), ACCESS_INPUT_VALUE).build())
        .returns(compilerType.getReturnType().getName())
        .build();
      
      return TypeSpec.interfaceBuilder(compilerType.getApi().getName())
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(annotation)
          .addSuperinterface(compilerType.getApi().getSuperinterface())
          .addMethod(apply)
          .addTypes(types);
    }
  }
}
