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

import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.ast.Locale_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.program.WorkflowProgramImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Workflow implements CompilableUnit {
  private final AST_Parser parser;
  private final Model<ArticleWorkflow> target;
  
  @Override
  public ArtifactLink compile(NewArtifact resolution) {
    
    
    final ArticleWorkflow_AST ast = parser.parseArticleWorkflow()
        .model(target)
        .onDependency(dep -> resolution.requireDependnecy(dep))
        .parse();
    resolution.ast(ast).id(target.getId()).name(ast.getName()).build();
    
    return new ArtifactLink() {
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public RuntimeLink accept(Artifact artifact) {
        
        return (runtime) -> {
          final var labels = new HashMap<String, String>(target.getBody()
              .getLabels().stream()
              .collect(Collectors.toMap(l -> l.getLocale(), l -> l.getLabelValue())));
          
          for(final var child : artifact.getChildDeps()) {
            if(child.getType() == BodyType.LOCALE) {
              final var labelValue = labels.get(child.getDependencyId());
              final var locale = (Locale_AST) child.getArtifactAst().get();
              labels.remove(child.getDependencyId());
              labels.put(locale.getLocaleCode(), labelValue);
            }
          }
          
          return new WorkflowProgramImpl(
              runtime,
              Collections.unmodifiableMap(labels),
              target, ast, 
              artifact.getErrors().isEmpty() ? artifact.getProgramStatus() : ProgramStatus.ERROR,
              artifact.getErrors(), 
              artifact.getAssociations());
        };
      }
    }; 
  }
}
