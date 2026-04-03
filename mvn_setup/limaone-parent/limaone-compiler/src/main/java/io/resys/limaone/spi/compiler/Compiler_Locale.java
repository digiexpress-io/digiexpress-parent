package io.resys.limaone.spi.compiler;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.ast.ImmutableLocale_AST;
import io.resys.limaone.ast.Locale_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Locale implements CompilableUnit {
  private final Model<Locale> target;

  @Override
  public ArtifactLink compile(NewArtifact resolution) {    

    final var ast = ImmutableLocale_AST.builder()
      .bodyType(BodyType.LOCALE)
      .name(target.getId())
      .hash(target.getBodyHash())
      .headers(ImmutableHeaders_AST.builder().build())
      .localeCode(target.getBody().getValue())
      .build();
    
    resolution.ast(ast).id(target.getId()).name(target.getId()).build();
    
    return new ArtifactLink() {
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public RuntimeLink accept(Artifact artifact) {
        return (runtime) -> {
          return new LocaleProgram(
              runtime,
              target, ast, 
              artifact.getErrors().isEmpty() ? artifact.getProgramStatus() : ProgramStatus.ERROR,
              artifact.getErrors(), 
              artifact.getAssociations());
          };
        };
    }; 
  }
  
  
  @Getter
  @RequiredArgsConstructor
  private static class LocaleProgram implements Program {
    private static final long serialVersionUID = 6217397271337733072L;
    private final io.resys.limaone.program.Runtime runtime;
    private final Model<Locale> target;
    private final Locale_AST ast;
    private final ProgramStatus status;
    private final List<ModelError> errors;
    private final List<ProgramAssociation> associations;
    
    @Override
    public String getId() {
      return target.getId();
    }
    @Override
    public String getName() {
      return ast.getName();
    }
    @Override
    public BodyType getType() {
      return ast.getBodyType();
    }
    @Override
    public List<String> getLocales() {
      return Arrays.asList(target.getBody().getValue());
    }
    @Override
    public List<Parameter> getHeaders() {
      return Collections.emptyList();
    }
  }
}
