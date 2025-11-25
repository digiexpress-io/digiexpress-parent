package io.resys.thena.ledger.client.spi.actions;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.ledger.client.api.ImmutableManyUnitPricesEnvelope;
import io.resys.thena.ledger.client.api.LedgerCommitActions.CreateManyUnitPrices;
import io.resys.thena.ledger.client.api.LedgerCommitActions.ManyUnitPricesEnvelope;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewUnitPrice;
import io.resys.thena.ledger.client.entities.ImmutableCommit;
import io.resys.thena.ledger.client.entities.UnitPrice;
import io.resys.thena.ledger.client.spi.actions.CreateOneLedgerImpl.CreateOneLedgerException;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.spi.create.NewUnitPriceBuilder;
import io.resys.thena.ledger.client.tables.BbDb;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateManyUnitPricesImpl implements CreateManyUnitPrices {

  private final BbDb state;
  private final String tenantId;
  
  private String author;
  private String message;
  private List<Consumer<NewUnitPrice>> ledger = new ArrayList<>();
  private Consumer<List<UnitPrice>> handleNewState;
  
  @Override
  public CreateManyUnitPrices commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  
  @Override
  public CreateManyUnitPrices commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  
  @Override
  public CreateManyUnitPrices addUnitPrice(Consumer<NewUnitPrice> addUnitPrice) {
    RepoAssert.notNull(addUnitPrice, () -> "addUnitPrice can't be empty!");
    ledger.add(addUnitPrice);
    return this;
  }

  @Override
  public Uni<ManyUnitPricesEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(ledger.isEmpty(), () -> "unitPrices can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withTransaction(scope, this::doInTx);
  }
  
  @Override
  public CreateManyUnitPrices onNewUnitPrices(Consumer<List<UnitPrice>> handleNewState) {
    this.handleNewState = handleNewState;
    return this;
  }

  private Uni<ManyUnitPricesEnvelope> doInTx(BbDb tx) {
    return createRequest(tx)
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(ManyUnitPricesEnvelopeException.class).recoverWithItem(ex -> {
          final ManyUnitPricesEnvelopeException error = (ManyUnitPricesEnvelopeException) ex;          
          return ImmutableManyUnitPricesEnvelope.builder()
            .repoId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'").append(" is rejected.")
                  .append(System.lineSeparator())
                  .append("Message: ").append(error.getMessage())
                  .toString())
                .exception(error)
                .build())
            .status(CommitResultStatus.ERROR)
          .build();
        });
  }
  
  private Uni<ManyUnitPricesEnvelope> createResponse(BbDb tx, PersistenceUnit request) {
    return tx.builder().from(request).persist().onItem().transform(rsp -> {
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new CreateOneLedgerException("Failed to create unit prices!", rsp);
      }
      
      final ManyUnitPricesEnvelope result = ImmutableManyUnitPricesEnvelope.builder()
          .repoId(tenantId)
          .addAllUnitPrices(request.getUnitPriceInserts())
          .addAllMessages(rsp.getCommitLogs().stream().map(log -> ImmutableMessage.builder()
              .exception(log.getException())
              .text(log.getText())
              .build()).toList())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
      return result;
    })
    .onItem().invoke(newState -> {
      if(handleNewState != null) {
        handleNewState.accept(newState.getUnitPrices());
      }
    });
  }
  
  private Uni<ImmutablePersistenceUnit> createRequest(BbDb tx) {
    final var start = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    


    final var logger = new LedgerCommitBuilder(tenantId, 
        ImmutableCommit.builder()
          .id(OidUtils.genUUID())
          .commitAuthor(author)
          .commitMessage(message)
          .commitLog("")
          .createdAt(createdAt)
          .ledgerId(Optional.empty())
          .build(),
        Optional.empty()
    );
    
    
    final var next = ImmutablePersistenceUnit.builder().from(start);
    for(final var newUnitPrice : this.ledger) {
      
      final var builder = new NewUnitPriceBuilder(logger, start, null);
      newUnitPrice.accept(builder);
      final var created = builder.close();  
      next.addUnitPriceInserts(created);
    }
  
    return Uni.createFrom().item(next.from(logger.close()).build());
  }
  
  public static class ManyUnitPricesEnvelopeException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final PersistenceUnit batch;
    
    public ManyUnitPricesEnvelopeException(String message, PersistenceUnit batch) {
      super(message + System.lineSeparator() + " " +
          String.join(System.lineSeparator() + " ", batch.getCommitLogs().stream().map(e -> e.getText()).toList()));
      
      batch.getCommitLogs().stream().filter(e -> e.getException() != null).forEach(e -> addSuppressed(e.getException()));
      this.batch = batch;
    }
    
    public PersistenceUnit getBatch() {
      return batch;
    }
  }
}
