package io.resys.limaone.spi.bundler;

import java.time.OffsetDateTime;
import java.util.List;

import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.ArticleProgram;
import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.program.Compiler.BundleQuery;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.WorkflowProgram;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.FlowTaskProgram;
import io.resys.limaone.program.Program;
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
  private final BundleGroup<WorkflowProgram> dialobs;
  
  
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
    final var dialobs = new BundleGroup<WorkflowProgram>(BodyType.DIALOB);
    
    programs.stream().forEach(program -> {
      articles.accept(program);
      flows.accept(program);
      flowTasks.accept(program);
      decisions.accept(program);
      dialobs.accept(program);
    });
    
    this.articles = articles.close();
    this.flows = flows.close();
    this.flowTasks = flowTasks.close();
    this.decisions = decisions.close();
    this.dialobs = dialobs.close();
  }

  @Override
  public BundleQuery<WorkflowProgram> queryDialob() {
    return new BundleQueryImpl<>(dialobs);
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
}
