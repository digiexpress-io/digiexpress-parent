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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerType;
import io.resys.hdes.executor.api.TraceBody.Accepts;
import io.resys.hdes.executor.api.TraceBody.Returns;

public class ImmutableSpec {

  public static ImmutableBuilder builder() {
    return new ImmutableBuilder();
  }
  
  public static ClassName from(ClassName src) {
    String pkg = src.packageName();
    int index = pkg.lastIndexOf(".");
    
    String top;
    if(index > -1 && Character.isUpperCase(pkg.charAt(index + 1))) {
      top = pkg.substring(0, index);
    } else {
      top = pkg;
    }
    
    return ClassName.get(top, "Immutable" + src.simpleName());
  }
  
  public static ClassName immutableBuilder(ClassName src) {
    ClassName type = from(src);
    return ClassName.get(type.packageName(), type.simpleName() + ".Builder");
  }
  
  public static ImmutableBuilder builder(ClassName name) {
    return new ImmutableBuilder().name(name);
  }
  
  public static ImmutableBuilder inputValue(CompilerType type) {
    return inputValue(type.getAccepts().getName());
  }
  
  public static ImmutableBuilder inputValue(ClassName type) {
    return new ImmutableBuilder().name(type).superinterface(Accepts.class);
  }

  public static ImmutableBuilder outputValue(ClassName type) {
    return new ImmutableBuilder().superinterface(Returns.class).name(type);
  }
  
  public static ImmutableBuilder outputValue(CompilerType type) {
    return outputValue(type.getReturns().getName());
  }

  public static class ImmutableBuilder {
    private final List<MethodSpec> methods = new ArrayList<>();
    private final List<TypeName> superinterfaces = new ArrayList<>();
    private ClassName name;
    private Consumer<TypeSpec> callback;
    
    private ImmutableBuilder() {
      super();
      this.callback = t -> {};
    }
    
    public ImmutableBuilder callback(Consumer<TypeSpec> callback) {
      this.callback = callback;
      return this;
    }
    
    public MethodBuilder method() {
      ImmutableBuilder that = this;
      return new MethodBuilder() {
        @Override
        public ImmutableBuilder build() {
          methods.add(super.buildInnerType());
          return that;
        }
      };
    }
    
    public MethodBuilder method(ScalarDef scalar) {
      return this.method()
          .name(scalar.getName())
          .isOptional(!scalar.getRequired())
          .isList(scalar.getArray())
          .returns(scalar.getType());
    }
    public MethodBuilder method(ObjectDef object, TypeName returns) {      
      return this.method()
          .name(object.getName())
          .isOptional(!object.getRequired())
          .isList(object.getArray())
          .returns(returns);
    }
    
    public MethodBuilder method(String name) {
      return this.method().name(name);
    }
    
    public MethodBuilder method(SimpleInvocation from) {
      return this.method().name(from);
    }
    
    public ImmutableBuilder superinterface(TypeName typeName) {
      this.superinterfaces.add(typeName);
      return this;
    }
    
    public ImmutableBuilder superinterface(Class<?> typeName) {
      this.superinterfaces.add(ClassName.get(typeName));
      return this;
    }
    
    public ImmutableBuilder name(ClassName name) {
      this.name = name;
      return this;
    }
    
    public TypeSpec build() {
      Assertions.notNull(name, () -> "name must be defined!");

      ClassName immutable = from(name);
      TypeSpec result = TypeSpec.interfaceBuilder(name)
        .addSuperinterfaces(superinterfaces)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addMethods(methods)
        .addAnnotation(Immutable.class)
        .addAnnotation(AnnotationSpec
            .builder(ClassName.get(Value.Style.class))
            .addMember("jdkOnly", "true")
            .build())
        .addAnnotation(AnnotationSpec
            .builder(ClassName.get("com.fasterxml.jackson.databind.annotation", "JsonSerialize"))
            .addMember("as", "$T.class", immutable)
            .build())
        .addAnnotation(AnnotationSpec
            .builder(ClassName.get("com.fasterxml.jackson.databind.annotation", "JsonDeserialize"))
            .addMember("as", "$T.class", immutable)
            .build())
        .build();    
    
      callback.accept(result);
      return result;
    }
    
  }
  
  
  public abstract static class MethodBuilder {
    private String name;
    private TypeName returns;
    private boolean isList;
    private boolean isOptional;
    private boolean isNullable;
    
    public MethodBuilder name(SimpleInvocation from) {
      this.name = JavaSpecUtil.getMethodName(from.getValue());
      return this;
    }
    
    public MethodBuilder name(String from) {
      this.name = JavaSpecUtil.getMethodName(from);
      return this;
    }
    
    public MethodBuilder returns(ScalarType type) {
      Class<?> javaType = JavaSpecUtil.type(type);
      this.returns = ClassName.get(javaType);    
      return this;
    }
    
    public MethodBuilder returns(TypeName type) {
      this.returns = type;
      return this;
    }
    
    public MethodBuilder isList() {
      this.isList = true;
      return this;
    }
    
    public MethodBuilder isList(boolean isList) {
      this.isList = isList;
      return this;
    }
    
    public MethodBuilder isOptional() {
      this.isOptional = true;
      return this;
    }
    
    public MethodBuilder isOptional(boolean isOptional) {
      this.isOptional = isOptional;
      return this;
    }
    
    public MethodBuilder isNullable(boolean isNullable) {
      this.isNullable = isNullable;
      return this;
    }
    
    private MethodSpec buildInnerType() {
      Assertions.notNull(name, () -> "name must be defined!");
      Assertions.notNull(returns, () -> "returns must be defined!");
      
      final TypeName returnType;
      if(isList) {
        returnType = ParameterizedTypeName.get(ClassName.get(List.class), returns);
      } else if(isOptional) {
        returnType = ParameterizedTypeName.get(ClassName.get(Optional.class), (ClassName) returns);  
      } else {
        returnType = returns;
      }
      
      final var method = MethodSpec.methodBuilder(name)
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(returnType);
      
      if(isNullable) {
        method.addAnnotation(AnnotationSpec.builder(Nullable.class).build());
      }
      return method.build();
    }
    
    public abstract ImmutableBuilder build();
  }
  

  
}
