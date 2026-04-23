package io.resys.limaone.spi.bundler;

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

import java.time.OffsetDateTime;
import java.util.List;

import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.ArticleProgram;
import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.program.Compiler.BundleQuery;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.DialobProgram;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.FlowTaskProgram;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.TagomiProgram;
import io.resys.limaone.program.WorkflowProgram;
import lombok.Getter;


@Getter
public class ImmutableBundle implements Bundle {
  private final String id;
  private final String name;
  private final String externalId;
  private final OffsetDateTime created;
  private final OffsetDateTime startDate;
  private final OffsetDateTime endDate;
  
  private final BundleGroup<ArticleProgram> articles;
  private final BundleGroup<FlowProgram> flows;
  private final BundleGroup<FlowTaskProgram> flowTasks;
  private final BundleGroup<DecisionProgram> decisions;
  private final BundleGroup<WorkflowProgram> workflows;
  private final BundleGroup<DialobProgram> dialobs;
  private final BundleGroup<TagomiProgram> tagomis;
  
  
  public ImmutableBundle(
      String id, 
      String name, 
      String externalId,
      OffsetDateTime created, 
      OffsetDateTime startDate, 
      OffsetDateTime endDate,
      List<Program> programs) {
    super();
    this.id = id;
    this.name = name;
    this.externalId = externalId;
    this.created = created;
    this.startDate = startDate;
    this.endDate = endDate;
    
    final var articles = new BundleGroup<ArticleProgram>(BodyType.ARTICLE);
    final var flows = new BundleGroup<FlowProgram>(BodyType.FLOW);
    final var flowTasks = new BundleGroup<FlowTaskProgram>(BodyType.FLOW_TASK);
    final var decisions = new BundleGroup<DecisionProgram>(BodyType.DECISION_TABLE);
    final var workflows = new BundleGroup<WorkflowProgram>(BodyType.ARTICLE_WORKFLOW);
    final var dialobs = new BundleGroup<DialobProgram>(BodyType.DIALOB_FORM);
    final var tagomis = new BundleGroup<TagomiProgram>(BodyType.PRINTOUT);

    programs.stream().forEach(program -> {
      articles.accept(program);
      flows.accept(program);
      flowTasks.accept(program);
      decisions.accept(program);
      workflows.accept(program);
      dialobs.accept(program);
      tagomis.accept(program);
    });

    this.articles = articles.close();
    this.flows = flows.close();
    this.flowTasks = flowTasks.close();
    this.decisions = decisions.close();
    this.workflows = workflows.close();
    this.dialobs = dialobs.close();
    this.tagomis = tagomis.close();
  }

  @Override
  public BundleQuery<WorkflowProgram> queryWorkflows() {
    return new BundleQueryImpl<>(workflows);
  }
  @Override
  public BundleQuery<ArticleProgram> queryArticles() {
    return new BundleQueryImpl<>(articles);
  }
  @Override
  public BundleQuery<FlowTaskProgram> queryFlowTasks() {
    return new BundleQueryImpl<>(flowTasks);
  }
  @Override
  public BundleQuery<FlowProgram> queryFlows() {
    return new BundleQueryImpl<>(flows);
  }
  @Override
  public BundleQuery<DecisionProgram> queryDecisions() {
    return new BundleQueryImpl<>(decisions);
  }
  @Override
  public BundleQuery<DialobProgram> queryDialobs() {
    return new BundleQueryImpl<>(dialobs);
  }
  @Override
  public BundleQuery<TagomiProgram> queryTagomis() {
    return new BundleQueryImpl<>(tagomis);
  }
}
