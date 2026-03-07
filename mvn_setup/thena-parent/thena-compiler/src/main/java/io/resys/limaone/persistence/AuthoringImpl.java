package io.resys.limaone.persistence;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.immutables.value.Value;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AuthoringImpl implements Authoring {

  private final AuthoringConfig config;
  
  @Override
  public ModifyModel modifyModel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NewModel newModel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeleteModel deleteModel() {
    // TODO Auto-generated method stub
    return null;
  }

  
  public static AuthoringBuilder builder() {
    return new AuthoringBuilder();
  }
  public static class AuthoringBuilder {
    private WorldPersistence persistence;
    private ScheduledExecutorService workerPool;
    private AST_Parser astParser;
    
    public AuthoringBuilder persistence(WorldPersistence persistence) {
      this.persistence = persistence;
      return this;
    }    
    public AuthoringBuilder workerPool(ScheduledExecutorService workerPool) {
      this.workerPool = workerPool;
      return this;
    }
    public AuthoringBuilder astParser(AST_Parser astParser) {
      this.astParser = astParser;
      return this;
    }
    
    public AuthoringImpl build() {
      Objects.requireNonNull(persistence, () -> "persistence must be defined");
      
      final var workerPool = this.workerPool == null ? Infrastructure.getDefaultWorkerPool() : this.workerPool;
      final var astParser = this.astParser == null ? AST_ParserImpl.builder().dev(true).build() : this.astParser;

      return new AuthoringImpl(ImmutableAuthoringConfig.builder()
          .workerTimeout(Duration.ofMinutes(15))
          .workerPool(workerPool)
          .astParser(astParser)
          .persistence(persistence)
          .build());
    }
  }

  @Value.Immutable
  interface AuthoringConfig {
    WorldPersistence getPersistence();
    ScheduledExecutorService getWorkerPool();
    Duration getWorkerTimeout();
    AST_Parser getAstParser();
  }
}
