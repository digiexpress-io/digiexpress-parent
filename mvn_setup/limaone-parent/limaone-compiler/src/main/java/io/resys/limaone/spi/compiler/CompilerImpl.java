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

import java.util.function.Function;
import java.util.stream.Stream;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.Compiler;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.limaone.spi.bundler.BundlerImpl;
import io.resys.limaone.spi.compiler.CompilableUnit.Bundler;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class CompilerImpl implements Compiler {

  private final EnvironmentProperties properties;
  
  @Override
  public AST_Parser getParser() {
    return properties.getAstParser();
  }
  
  @Override
  public BundleBuilder compile(ModelWorld world) {

    final var workerPool = properties.getWorkerPool(); 
    final var maxTimeout = properties.getWorkerPoolMaxTimeout(); 
    final var astParser = properties.getAstParser();
    
    final Bundler bundler = new BundlerImpl(properties);
    
    // locales
    final Stream<CompilableUnit> locales = world.getLocales().values().stream()
        .map(locale -> new Compiler_Locale(locale));
    
    // forms
    final Stream<CompilableUnit> forms = world.getForms().values().stream()
        .map(form -> new Compiler_Dialob(astParser, form));
    
    // workflows
    final Stream<CompilableUnit> workflows = world.getArticleWorkflows().values().stream()
        .map(wk -> new Compiler_Workflow(astParser, wk));

    // main stencil article
    final Stream<CompilableUnit> article = world.getArticles().isEmpty() && world.getArticleWorkflows().isEmpty() ?
        Stream.empty() : Stream.of(new Compiler_Article(astParser, world));
    
    // wrench flows
    final Stream<CompilableUnit> flows = world.getFlows().values().stream()
        .map(f -> new Compiler_Flow(astParser, world, f));
    
    // wrench flow tasks
    final Stream<CompilableUnit> flowTasks = world.getFlowTasks().values().stream()
        .map(f -> new Compiler_FlowTask(astParser, world, f));
    
    // wrench dt
    final Stream<CompilableUnit> decisions = world.getDecisionTables().values().stream()
        .map(f -> new Compiler_DecisionTable(astParser, world, f));

    // tagomi printout
    final Stream<CompilableUnit> tagomis = world.getPrintouts().values().stream()
        .map(p -> new Compiler_Tagomi(astParser, world, p));

    // combine all to single stream
    final Stream<CompilableUnit> itemsToCompile =  Stream.of(locales, forms, workflows, article, flows, flowTasks, decisions, tagomis)
        .flatMap(Function.identity());
    
    // bundle all 
    return Multi.createFrom().items(itemsToCompile)
      .onItem().transform(unit -> unit.compile(bundler.newArtifact()))
      .runSubscriptionOn(workerPool)
      .collect().asList().onItem().transformToUni(bundler::build)
      .await().atMost(maxTimeout)
      .id(world.getName());
  }
}
