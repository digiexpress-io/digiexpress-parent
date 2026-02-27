package io.resys.limaone.spi.compiler;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Stream;

import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.Compiler;
import io.resys.limaone.spi.compiler.CompilableUnit.BundleBuilder;
import io.resys.limaone.spi.resolution.BundleBuilder_Impl;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class Compiler_Impl implements Compiler {

  private final ScheduledExecutorService workerPool;
  private final Duration maxTimeout;
  
  @Override
  public Bundle compile(ModelWorld world) {
    
    final BundleBuilder bundler = new BundleBuilder_Impl();
    
    // dialobs
    final Stream<CompilableUnit> dialobs = world.getArticleWorkflows()
      .values().stream().map(wk -> new Compiler_Dialob(world, wk));

    // main stencil article
    final Stream<CompilableUnit> article = Stream.of(new Compiler_Article(world));
    
    // wrench flows
    final Stream<CompilableUnit> flows = world.getFlows().values().stream().map(f -> new Compiler_Flow(world, f));
    
    // wrench flow tasks
    final Stream<CompilableUnit> flowTasks = world.getFlowTasks().values().stream().map(f -> new Compiler_FlowTask(world, f));
    
    // wrench dt
    final Stream<CompilableUnit> decisions = world.getDecisionTables().values().stream().map(f -> new Compiler_DecisionTable(world, f));
    
    // combine all to single stream
    final Stream<CompilableUnit> itemsToCompile =  Stream.of(dialobs, article, flows, flowTasks, decisions).flatMap(Function.identity());
    
    // bundle all 
    return Multi.createFrom().items(itemsToCompile)
      .onItem().transform(unit -> unit.compile(bundler.newArtifact()))
      .runSubscriptionOn(this.workerPool)
      .collect().asList().onItem().transformToUni(bundler::build)
      .await().atMost(maxTimeout);
  }

  
  public static class CompilerBuilder {
    private ScheduledExecutorService workerPool;
    
    public Compiler_Impl build() {
      final var workerPool = this.workerPool == null ? Infrastructure.getDefaultWorkerPool() : this.workerPool;
      return new Compiler_Impl(workerPool, Duration.ofMinutes(15));
    }
  }
  
  public static CompilerBuilder builder() {
    return new CompilerBuilder();
  }
}
