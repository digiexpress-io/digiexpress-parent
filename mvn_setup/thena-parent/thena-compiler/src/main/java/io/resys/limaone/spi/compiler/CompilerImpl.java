package io.resys.limaone.spi.compiler;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Stream;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.Compiler;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.bundler.BundlerImpl;
import io.resys.limaone.spi.compiler.CompilableUnit.Bundler;
import io.resys.limaone.spi.compiler.article.Compiler_Article;
import io.resys.limaone.spi.compiler.flow.Compiler_Flow;
import io.resys.limaone.spi.compiler.flowtask.Compiler_FlowTask;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class CompilerImpl implements Compiler {

  private final ScheduledExecutorService workerPool;
  private final Duration maxTimeout;
  private final AST_Parser astParser;
  
  
  @Override
  public BundleBuilder compile(ModelWorld world) {
    
    final Bundler bundler = new BundlerImpl();
    
    // dialobs
    final Stream<CompilableUnit> dialobs = world.getArticleWorkflows()
      .values().stream().map(wk -> new Compiler_Dialob(world, wk));

    // main stencil article
    final Stream<CompilableUnit> article = Stream.of(new Compiler_Article(astParser, world));
    
    // wrench flows
    final Stream<CompilableUnit> flows = world.getFlows().values().stream().map(f -> new Compiler_Flow(astParser, world, f));
    
    // wrench flow tasks
    final Stream<CompilableUnit> flowTasks = world.getFlowTasks().values().stream().map(f -> new Compiler_FlowTask(astParser, world, f));
    
    // wrench dt
    final Stream<CompilableUnit> decisions = world.getDecisionTables().values().stream().map(f -> new Compiler_DecisionTable(astParser, world, f));
    
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
    private AST_Parser astParser;
    
    public CompilerBuilder workerPool(ScheduledExecutorService workerPool) {
      this.workerPool = workerPool;
      return this;
    }
    public CompilerBuilder astParser(AST_Parser astParser) {
      this.astParser = astParser;
      return this;
    }
    
    public CompilerImpl build() {
      final var workerPool = this.workerPool == null ? Infrastructure.getDefaultWorkerPool() : this.workerPool;
      final var astParser = this.astParser == null ? AST_ParserImpl.builder().dev(false).build() : this.astParser;
      return new CompilerImpl(workerPool, Duration.ofMinutes(15), astParser);
    }
  }
  
  public static CompilerBuilder builder() {
    return new CompilerBuilder();
  }
}
