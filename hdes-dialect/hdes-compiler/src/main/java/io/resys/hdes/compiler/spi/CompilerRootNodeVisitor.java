package io.resys.hdes.compiler.spi;

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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.googlejavaformat.java.Formatter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;
import io.resys.hdes.ast.api.nodes.HdesTree.FlowTree;
import io.resys.hdes.ast.api.nodes.HdesTree.RootTree;
import io.resys.hdes.ast.api.nodes.HdesTree.ServiceTree;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.api.visitors.HdesVisitor.RootNodeVisitor;
import io.resys.hdes.ast.spi.ImmutableHdesTree;
import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.compiler.api.HdesCompiler.ResourceName;
import io.resys.hdes.compiler.api.HdesCompiler.ResourceType;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.api.ImmutableResource;
import io.resys.hdes.compiler.api.ImmutableResourceDeclaration;
import io.resys.hdes.compiler.api.ImmutableResourceName;
import io.resys.hdes.compiler.spi.dt.visitors.DtApiVisitor;
import io.resys.hdes.compiler.spi.dt.visitors.DtImplVisitor;
import io.resys.hdes.compiler.spi.fl.visitors.FlApiVisitor;
import io.resys.hdes.compiler.spi.fl.visitors.FlImplVisitor;
import io.resys.hdes.compiler.spi.st.visitors.StApiVisitor;
import io.resys.hdes.compiler.spi.st.visitors.StImplVisitor;
import io.resys.hdes.compiler.spi.st.visitors.StPromiseApiVisitor;
import io.resys.hdes.compiler.spi.st.visitors.StPromiseImplVisitor;
import io.resys.hdes.compiler.spi.units.CompilerNode;
import io.resys.hdes.compiler.spi.units.CompilerNode.DecisionTableUnit;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;
import io.resys.hdes.compiler.spi.units.CompilerNode.ServiceUnit;
import io.resys.hdes.compiler.spi.units.ImmutableCompilerNode;

public class CompilerRootNodeVisitor implements RootNodeVisitor<List<Resource>, Resource> {
  
  @Override
  public List<Resource> visitBody(RootNode root) {
    final CompilerNode compilerNode = ImmutableCompilerNode.config().ast(root).build();
    final RootTree ctx = (RootTree) ImmutableHdesTree.builder().value(root).parent(ImmutableHdesTree.builder().value(compilerNode).build()).build();
    
    return Collections.unmodifiableList(root
        .getBody().values().stream()
        .map(node -> visitNode(node, ctx))
        .collect(Collectors.toList()));
  }

  @Override
  public Resource visitNode(BodyNode ast, RootTree ctx) {
    if (ast instanceof DecisionTableBody) {
      final DecisionTableUnit type = ctx.get().node(CompilerNode.class).dt((DecisionTableBody) ast);
      return visitDecisionTable((DecisionTableTree) ctx.next(type).next(ast));
    } else if (ast instanceof FlowBody) {
      final FlowUnit unit = ctx.get().node(CompilerNode.class).fl((FlowBody) ast);
      return visitFlow((FlowTree) ctx.next(unit).next(ast));
    } else if (ast instanceof ServiceBody) {
      final ServiceUnit unit = ctx.get().node(CompilerNode.class).st((ServiceBody) ast);
      return visitService((ServiceTree) ctx.next(unit).next(ast));
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(ast));
    }
  }

  @Override
  public Resource visitService(ServiceTree ctx) {
    final var body = ctx.getValue();
    final var type = ctx.get().node(ServiceUnit.class);
    final var promise = body.getCommand().getPromise().isPresent();
    
    final TypeSpec api = promise ? new StPromiseApiVisitor().visitBody(ctx) : new StApiVisitor().visitBody(ctx);    
    final TypeSpec impl = promise ? new StPromiseImplVisitor().visitBody(ctx) : new StImplVisitor().visitBody(ctx);
    final var pkg = type.getType().getPkg();

    return ImmutableResource.builder()
        .type(ResourceType.ST)
        .name(body.getId().getValue())
        .source(body.getToken().getText())
        .ast(body)
        
        .accepts(typeName(type.getType().getAccepts().getName()))
        .returns(typeName(type.getType().getReturns().getName()))
        .ends(typeName(type.getType().getReturnType().getName()))

        .addDeclarations(ImmutableResourceDeclaration.builder()
            .type(ImmutableResourceName.builder().name(api.name).pkg(pkg).build())
            .isExecutable(false).value(javaFile(api, pkg)).build())

        .addDeclarations(ImmutableResourceDeclaration.builder()
            .type(ImmutableResourceName.builder().name(impl.name).pkg(pkg).build())
            .isExecutable(true).value(javaFile(impl, pkg)).build())

        .build();
  }

  @Override
  public Resource visitFlow(FlowTree ctx) {
    final var body = ctx.getValue();
    final var type = ctx.get().node(FlowUnit.class);
    final TypeSpec api = new FlApiVisitor().visitBody(ctx);    
    final TypeSpec impl = new FlImplVisitor().visitBody(ctx);
    final var pkg = type.getType().getPkg();

    return ImmutableResource.builder()
        .type(ResourceType.FL)
        .name(body.getId().getValue())
        .source(body.getToken().getText())
        .ast(body)
        
        .accepts(typeName(type.getType().getAccepts().getName()))
        .returns(typeName(type.getType().getReturns().getName()))
        .ends(typeName(type.getType().getReturnType().getName()))

        .addDeclarations(ImmutableResourceDeclaration.builder()
            .type(ImmutableResourceName.builder().name(api.name).pkg(pkg).build())
            .isExecutable(false).value(javaFile(api, pkg)).build())

        .addDeclarations(ImmutableResourceDeclaration.builder()
            .type(ImmutableResourceName.builder().name(impl.name).pkg(pkg).build())
            .isExecutable(true).value(javaFile(impl, pkg)).build())

        .build();
  }

  @Override
  public Resource visitDecisionTable(DecisionTableTree ctx) {
    final var body = ctx.getValue();
    final var type = ctx.get().node(DecisionTableUnit.class);
    
    final TypeSpec api = new DtApiVisitor().visitBody(ctx);
    final TypeSpec impl = new DtImplVisitor().visitBody(ctx);
    
    final var pkg = type.getType().getPkg();
    final var nestedPkg = pkg + "." + api.name;
    
    return ImmutableResource.builder()
        .type(ResourceType.DT)
        .name(body.getId().getValue())
        .source(body.getToken().getText())
        .ast(body)
        .addAllTypes(api.typeSpecs.stream().map(spec -> ImmutableResourceName.builder().name(spec.name).pkg(nestedPkg).build()).collect(Collectors.toList()))
        
        .addTypes(ImmutableResourceName.builder().name(api.name).pkg(pkg).build())
        .addTypes(ImmutableResourceName.builder().name(impl.name).pkg(pkg).build())
        
        .accepts(typeName(type.getType().getAccepts().getName()))
        .returns(typeName(type.getType().getReturns().getName()))
        .ends(typeName(type.getType().getReturnType().getName()))
        
        .addDeclarations(ImmutableResourceDeclaration.builder()
            .type(ImmutableResourceName.builder().name(api.name).pkg(pkg).build())
            .isExecutable(false).value(javaFile(api, pkg)).build())
        
        .addDeclarations(ImmutableResourceDeclaration.builder()
            .type(ImmutableResourceName.builder().name(impl.name).pkg(pkg).build())
            .isExecutable(true).value(javaFile(impl, pkg)).build())
        
        .build();
  }
  
  
  private static ResourceName typeName(ClassName name) {
    return ImmutableResourceName.builder().name(name.simpleName()).pkg(name.packageName()).build();
  }
  
  public static String javaFile(TypeSpec spec, String pkg) {
    try {
      StringBuilder appendable = new StringBuilder();
      JavaFile file = JavaFile.builder(pkg, spec).build();
      file.writeTo(appendable);
      String result = appendable.toString();
      return new Formatter().formatSource(result);
    } catch (Exception e) {
      
      StringBuilder appendable = new StringBuilder();
      JavaFile file = JavaFile.builder(pkg, spec).build();
      
      try {
        file.writeTo(appendable);
      } catch(IOException e1) {
        throw new UncheckedIOException(e1);
      }
      String result = appendable.toString();
      return result;
    }
  }
}
