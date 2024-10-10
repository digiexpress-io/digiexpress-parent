package io.resys.hdes.compiler.spi.spec;

import java.io.Serializable;

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
import java.util.Optional;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.compiler.api.HdesCompiler.ResourceType;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerType;
import io.resys.hdes.executor.api.Trace.TracePromise;

public class HdesDefPromiseSpec {
  
  public final static String ACCESS_DATA_ID = "dataId";
  public final static String ACCESS_DATA = "data";
  public final static String ACCESS_TIMEOUT = "timeout";
  
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
    private CodeBlock onEnter;
    private CodeBlock onComplete;
    private CodeBlock onTimeout;
    private CodeBlock onError;

    private ImplBuilder(CompilerType compilerType) {
      super();
      this.compilerType = compilerType;
    }
    
    public ImplBuilder onEnter(CodeBlock onEnter) {
      this.onEnter = onEnter;
      return this;
    }
    public ImplBuilder onComplete(CodeBlock onComplete) {
      this.onComplete = onComplete;
      return this;
    }
    public ImplBuilder onTimeout(CodeBlock onTimeout) {
      this.onTimeout = onTimeout;
      return this;
    }
    public ImplBuilder onError(CodeBlock onError) {
      this.onError = onError;
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
      
      
      List<AnnotationSpec> annotations = Arrays.asList(
          AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build(),
          AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", HdesDefPromiseSpec.class.getCanonicalName()).build());
      
      MethodSpec onEnter = MethodSpec.methodBuilder("onEnter")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), HdesDefSpec.ACCESS_INPUT_VALUE).build())
        .addCode(Optional.ofNullable(this.onEnter).orElseGet(() -> CodeBlock.builder().addStatement("return null").build()))
        .returns(TracePromise.class)
        .build();
      
      MethodSpec onComplete = MethodSpec.methodBuilder("onComplete")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ParameterSpec.builder(String.class, ACCESS_DATA_ID).build())
          .addParameter(ParameterSpec.builder(Serializable.class, ACCESS_DATA).build())
          .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), HdesDefSpec.ACCESS_INPUT_VALUE).build())
          
          .addCode(Optional.ofNullable(this.onComplete).orElseGet(() -> CodeBlock.builder().addStatement("return null").build()))
          .returns(compilerType.getReturnType().getName())
          .build();
      
      MethodSpec onError = MethodSpec.methodBuilder("onError")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ParameterSpec.builder(String.class, ACCESS_DATA_ID).build())
          .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), HdesDefSpec.ACCESS_INPUT_VALUE).build())
          .addCode(Optional.ofNullable(this.onError).orElseGet(() -> CodeBlock.builder().addStatement("return null").build()))
          .returns(compilerType.getReturnType().getName())
          .build();
      
            
      MethodSpec onTimeout = MethodSpec.methodBuilder("onTimeout")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ParameterSpec.builder(String.class, ACCESS_DATA_ID).build())
          .addParameter(ParameterSpec.builder(long.class, ACCESS_TIMEOUT).build())
          .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), HdesDefSpec.ACCESS_INPUT_VALUE).build())
          .addCode(Optional.ofNullable(this.onTimeout).orElseGet(() -> CodeBlock.builder().addStatement("return null").build()))
          .returns(compilerType.getReturnType().getName())
          .build();
      return TypeSpec.classBuilder(compilerType.getImpl().getName())
          .addModifiers(Modifier.PUBLIC)
          .addAnnotations(annotations)
          .addSuperinterface(compilerType.getImpl().getSuperinterface())
          .addFields(fields)
          .addMethod(onEnter)
          .addMethod(onComplete)
          .addMethod(onError)
          .addMethod(onTimeout)
          .addMethods(this.methods);
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
          .addMember("value", "$S", HdesDefPromiseSpec.class.getCanonicalName()).build();
      
      MethodSpec onEnter = MethodSpec.methodBuilder("onEnter")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), HdesDefSpec.ACCESS_INPUT_VALUE).build())
        .returns(TracePromise.class)
        .build();
      
      MethodSpec onComplete = MethodSpec.methodBuilder("onComplete")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .addParameter(ParameterSpec.builder(String.class, ACCESS_DATA_ID).build())
          .addParameter(ParameterSpec.builder(Serializable.class, ACCESS_DATA).build())
          .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), HdesDefSpec.ACCESS_INPUT_VALUE).build())
          .returns(compilerType.getReturnType().getName())
          .build();
      
      MethodSpec onError = MethodSpec.methodBuilder("onError")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .addParameter(ParameterSpec.builder(String.class, ACCESS_DATA_ID).build())
          .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), HdesDefSpec.ACCESS_INPUT_VALUE).build())
          .returns(compilerType.getReturnType().getName())
          .build();
      
      MethodSpec onTimeout = MethodSpec.methodBuilder("onTimeout")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .addParameter(ParameterSpec.builder(String.class, ACCESS_DATA_ID).build())
          .addParameter(ParameterSpec.builder(long.class, ACCESS_TIMEOUT).build())
          .addParameter(ParameterSpec.builder(compilerType.getAccepts().getName(), HdesDefSpec.ACCESS_INPUT_VALUE).build())
          .returns(compilerType.getReturnType().getName())
          .build();
      
      
      return TypeSpec.interfaceBuilder(compilerType.getApi().getName())
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(annotation)
          .addSuperinterface(compilerType.getApi().getSuperinterface())
          .addMethod(onEnter)
          .addMethod(onComplete)
          .addMethod(onError)
          .addMethod(onTimeout)
          .addTypes(types);
    }
  }
}
