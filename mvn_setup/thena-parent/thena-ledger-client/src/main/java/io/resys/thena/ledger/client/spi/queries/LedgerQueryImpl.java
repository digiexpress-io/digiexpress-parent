package io.resys.thena.ledger.client.spi.queries;

/*-
 * #%L
 * thena-ledger-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.ledger.client.api.LedgerQueryActions.LedgerQuery;
import io.resys.thena.ledger.client.api.ImmutableLedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.entities.LedgerDocType;
import io.resys.thena.ledger.client.tables.BbDb;
import io.resys.thena.ledger.client.tables.BbDbQuery;
import io.resys.thena.ledger.client.tables.ImmutableLedgerTableFilter;
import io.resys.thena.ledger.client.tables.ImmutableWorld;
import io.resys.thena.ledger.client.tables.LedgerTableFilter;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LedgerQueryImpl implements LedgerQuery {

  private final Uni<BbDb> state;
  
  private List<String> ledgerIds;
  private final List<LedgerDocType> excludedDocs = new ArrayList<>();
  private boolean lockForUpdate;

  @Override
  public LedgerQuery lockForUpdate() {
    this.lockForUpdate = true;
    return this;
  }

  @Override
  public LedgerQuery excludeDocs(LedgerDocType... docs) {
    this.excludedDocs.addAll(Arrays.asList(docs));
    return this;
  }

  @Override
  public LedgerQuery addLedgerId(String id) {
    if(this.ledgerIds == null) {
      this.ledgerIds = new ArrayList<>();
    }
    this.ledgerIds.add(id);
    return this;
  }

  @Override
  public LedgerQuery addAllLedgerId(List<String> ids) {
    if(this.ledgerIds == null) {
      this.ledgerIds = new ArrayList<>();
    }
    this.ledgerIds.addAll(ids);
    return this;
  }

  private Uni<List<LedgerContainer>> startQuery(BbDb state) {
    final var query = ImmutableLedgerTableFilter.builder()
        .lockForUpdate(Boolean.TRUE.equals(this.lockForUpdate))
        .ledgerIds(Optional.ofNullable(this.ledgerIds == null || this.ledgerIds.isEmpty() ? null: this.ledgerIds))
        .build();

    return Uni.combine().all().unis(
        findAllLedgers(state, query),
        findAllMoneyRequests(state, query),
        findAllPayments(state, query),
        findAllSettlements(state, query),
        findAllSettlementPayments(state, query),
        findAllBlackBooks(state, query),
        findAllBlackBookDetails(state, query),
        findAllProjections(state, query),
        findAllProjectionDetails(state, query),
        findAllUnitPrices(state, query),
        findAllLedgerEvents(state, query)
      ).with(BbDbQuery.World.class, (containers) -> {
        final var combined = ImmutableWorld.builder();
        containers.forEach(container -> combined.from(container));
        final BbDbQuery.World built = combined.build();
        final var result = groupByLedger(built);
        return result;
      });
  }

  @Override
  public Uni<QueryEnvelope<LedgerContainer>> get(String ledgerIdOrExtId) {
    this.addLedgerId(ledgerIdOrExtId);
    return this.state
        .onItem().transformToUni(state -> {
          return startQuery(state)
              .onItem().transform((container) -> ImmutableQueryEnvelope.<LedgerContainer>builder()
                  .repo(state.getDataSource().getTenant())
                  .status(QueryEnvelopeStatus.OK)
                  .objects(container.isEmpty() ? null : container.getFirst())
                  .build());
        });
  }

  @Override
  public Uni<QueryEnvelopeList<LedgerContainer>> findAll() {
    return this.state
        .onItem().transformToUni(state -> {
          return startQuery(state)
              .onItem().transform(items -> ImmutableQueryEnvelopeList.<LedgerContainer>builder()
                  .repo(state.getDataSource().getTenant())
                  .status(QueryEnvelopeStatus.OK)
                  .objects(items)
                  .build());
        });
  }

  private Uni<BbDbQuery.World> findAllLedgers(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.LEDGER)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryLedger().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().ledger(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllMoneyRequests(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.MONEY_REQUEST)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryMoneyRequest().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().moneyRequest(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllPayments(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.PAYMENT)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryPayment().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().payment(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllSettlements(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.SETTLEMENT)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().querySettlement().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().settlement(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllSettlementPayments(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.SETTLEMENT_PAYMENT)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().querySettlementPayment().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().settlementPayment(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllBlackBooks(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.BLACK_BOOK)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryBlackBook().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().blackBook(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllBlackBookDetails(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.BLACK_BOOK_DETAIL)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryBlackBookDetail().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().blackBookDetail(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllProjections(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.PROJECTION)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryProjection().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().projection(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllProjectionDetails(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.PROJECTION_DETAIL)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryProjectionDetail().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().projectionDetail(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllUnitPrices(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.UNIT_PRICE)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryUnitPrice().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().unitPrice(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<BbDbQuery.World> findAllLedgerEvents(BbDb state, LedgerTableFilter filter) {
    if(this.excludedDocs.contains(LedgerDocType.LEDGER_EVENT)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryLedgerEvent().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().ledgerEvent(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }

  private List<LedgerContainer> groupByLedger(BbDbQuery.World world) {
    final Map<String, LedgerContainer.Builder> ledgerContainers = world.getLedger().values().stream()
        .collect(Collectors.toMap(
            ledger -> ledger.getId(),
            ledger -> ImmutableLedgerContainer.builder().ledger(ledger)
        ));

    // Group money requests by ledger ID
    world.getMoneyRequest().values().forEach(moneyRequest -> {
      final var container = ledgerContainers.get(moneyRequest.getLedgerId());
      if (container != null) {
        container.addMoneyRequests(moneyRequest);
      }
    });

    // Group payments by ledger ID
    world.getPayment().values().forEach(payment -> {
      final var container = ledgerContainers.get(payment.getLedgerId());
      if (container != null) {
        container.addPayments(payment);
      }
    });

    // Group settlements by ledger ID
    world.getSettlement().values().forEach(settlement -> {
      final var container = ledgerContainers.get(settlement.getLedgerId());
      if (container != null) {
        container.addSettlements(settlement);
      }
    });

    // Group settlement payments by settlement ID (need to find ledger through settlement)
    final Map<String, String> settlementToLedger = world.getSettlement().values().stream()
        .collect(Collectors.toMap(s -> s.getId(), s -> s.getLedgerId()));

    world.getSettlementPayment().values().forEach(settlementPayment -> {
      final var ledgerId = settlementToLedger.get(settlementPayment.getSettlementId());
      if (ledgerId != null) {
        final var container = ledgerContainers.get(ledgerId);
        if (container != null) {
          container.addSettlementPayments(settlementPayment.getSettlementId(), settlementPayment);
        }
      }
    });

    // Group black books by ledger ID
    world.getBlackBook().values().forEach(blackBook -> {
      final var container = ledgerContainers.get(blackBook.getLedgerId());
      if (container != null) {
        container.addBlackBooks(blackBook);
      }
    });

    // Group black book details by black book ID (need to find ledger through black book)
    final Map<String, String> blackBookToLedger = world.getBlackBook().values().stream()
        .collect(Collectors.toMap(bb -> bb.getId(), bb -> bb.getLedgerId()));

    world.getBlackBookDetail().values().forEach(blackBookDetail -> {
      final var ledgerId = blackBookToLedger.get(blackBookDetail.getBlackBookId());
      if (ledgerId != null) {
        final var container = ledgerContainers.get(ledgerId);
        if (container != null) {
          container.addBlackBookDetails(blackBookDetail.getBlackBookId(), blackBookDetail);
        }
      }
    });

    // Group projections by ledger ID
    world.getProjection().values().forEach(projection -> {
      final var container = ledgerContainers.get(projection.getLedgerId());
      if (container != null) {
        container.addProjections(projection);
      }
    });

    // Group projection details by projection ID (need to find ledger through projection)
    final Map<String, String> projectionToLedger = world.getProjection().values().stream()
        .collect(Collectors.toMap(p -> p.getId(), p -> p.getLedgerId()));

    world.getProjectionDetail().values().forEach(projectionDetail -> {
      final var ledgerId = projectionToLedger.get(projectionDetail.getProjectionId());
      if (ledgerId != null) {
        final var container = ledgerContainers.get(ledgerId);
        if (container != null) {
          container.addProjectionDetails(projectionDetail.getProjectionId(), projectionDetail);
        }
      }
    });

    // Unit prices are not ledger-specific, add to all containers
    world.getUnitPrice().values().forEach(unitPrice -> {
      ledgerContainers.values().forEach(container -> {
        container.addUnitPrices(unitPrice);
      });
    });

    // Group ledger events by ledger ID
    world.getLedgerEvent().values().forEach(ledgerEvent -> {
      final var container = ledgerContainers.get(ledgerEvent.getLedgerId());
      if (container != null) {
        container.addLedgerEvents(ledgerEvent);
      }
    });

    return ledgerContainers.values().stream()
        .map(LedgerContainer.Builder::build)
        .collect(Collectors.toList());
  }
}