package io.resys.thena.contract.client.spi.queries;

/*-
 * #%L
 * thena-contract-client
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
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.contract.client.api.ContractQueryActions.ContractQuery;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.entities.ContractDocType;
import io.resys.thena.contract.client.tables.ContractDb;
import io.resys.thena.contract.client.tables.ContractDbQuery;
import io.resys.thena.contract.client.tables.ContractTableFilter;
import io.resys.thena.contract.client.tables.ImmutableContractTableFilter;
import io.resys.thena.contract.client.tables.ImmutableWorld;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContractQueryImpl implements ContractQuery {

  private final Uni<ContractDb> state;
  
  private List<String> contractIds;
  private final List<ContractDocType> excludedDocs = new ArrayList<>();
  private boolean lockForUpdate;

  @Override
  public ContractQuery lockForUpdate() {
    this.lockForUpdate = true;
    return this;
  }

  @Override
  public ContractQuery excludeDocs(ContractDocType... docs) {
    this.excludedDocs.addAll(Arrays.asList(docs));
    return this;
  }

  @Override
  public ContractQuery addContractId(String id) {
    if(this.contractIds == null) {
      this.contractIds = new ArrayList<>();
    }
    this.contractIds.add(id);
    return this;
  }

  @Override
  public ContractQuery addAllContractId(List<String> ids) {
    if(this.contractIds == null) {
      this.contractIds = new ArrayList<>();
    }
    this.contractIds.addAll(ids);
    return this;
  }
  
  
  

  private Uni<List<ContractContainer>> startQuery(ContractDb state) {
    final var query = ImmutableContractTableFilter.builder()
        .lockForUpdate(Boolean.TRUE.equals(this.lockForUpdate))
        .contractIds(Optional.ofNullable(this.contractIds == null || this.contractIds.isEmpty() ? null: this.contractIds))
        .build();

    return Uni.combine().all().unis(
        findAllContracts(state, query),
        findAllParties(state, query),
        findAllCoverages(state, query),
        findAllReferences(state, query),
        findAllNotes(state, query),
        findAllCapabilities(state, query),
        findAllInvPlans(state, query),
        findAllPaymentPlans(state, query),
        findAllInvPlanAllocs(state, query)
      ).with(ContractDbQuery.World.class, (containers) -> {
        final var combined = ImmutableWorld.builder();
        containers.forEach(container -> combined.from(container));
        final ContractDbQuery.World built = combined.build();
        final var result = groupByContract(built);
        return result;
      });
  }

  @Override
  public Uni<QueryEnvelope<ContractContainer>> get(String contractIdOrExtId) {
    this.addContractId(contractIdOrExtId);
    return this.state
        .onItem().transformToUni(state -> {
          return startQuery(state)
              .onItem().transform((container) -> ImmutableQueryEnvelope.<ContractContainer>builder()
                  .repo(state.getDataSource().getTenant())
                  .status(QueryEnvelopeStatus.OK)
                  .objects(container.isEmpty() ? null : container.getFirst())
                  .build());
        });
  }

  @Override
  public Uni<QueryEnvelopeList<ContractContainer>> findAll() {
    return this.state
        .onItem().transformToUni(state -> {
          return startQuery(state)
              .onItem().transform(items -> ImmutableQueryEnvelopeList.<ContractContainer>builder()
                  .repo(state.getDataSource().getTenant())
                  .status(QueryEnvelopeStatus.OK)
                  .objects(items)
                  .build());
        });
  }
  
  
  
  private Uni<ContractDbQuery.World> findAllContracts(ContractDb state, ContractTableFilter filter) {
    if(this.excludedDocs.contains(ContractDocType.CONTRACT)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryContract().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().contract(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<ContractDbQuery.World> findAllParties(ContractDb state, ContractTableFilter filter) {
    if(this.excludedDocs.contains(ContractDocType.PARTY)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryParty().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().party(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<ContractDbQuery.World> findAllCoverages(ContractDb state, ContractTableFilter filter) {
    if(this.excludedDocs.contains(ContractDocType.COVERAGE)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryCoverage().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().coverage(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<ContractDbQuery.World> findAllReferences(ContractDb state, ContractTableFilter filter) {
    if(this.excludedDocs.contains(ContractDocType.REFERENCE)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryReference().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().reference(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<ContractDbQuery.World> findAllNotes(ContractDb state, ContractTableFilter filter) {
    if(this.excludedDocs.contains(ContractDocType.NOTE)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryNote().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().note(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<ContractDbQuery.World> findAllCapabilities(ContractDb state, ContractTableFilter filter) {
    if(this.excludedDocs.contains(ContractDocType.CAPABILITY)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryCapability().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().capability(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<ContractDbQuery.World> findAllInvPlans(ContractDb state, ContractTableFilter filter) {
    if(this.excludedDocs.contains(ContractDocType.INV_PLAN)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryInvPlan().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().invPlan(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<ContractDbQuery.World> findAllPaymentPlans(ContractDb state, ContractTableFilter filter) {
    if(this.excludedDocs.contains(ContractDocType.PAYMENT_PLAN)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryPaymentPlan().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().paymentPlan(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<ContractDbQuery.World> findAllInvPlanAllocs(ContractDb state, ContractTableFilter filter) {
    if(this.excludedDocs.contains(ContractDocType.INV_PLAN_ALLOC)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryInvPlanAlloc().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().invPlanAlloc(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  public static ContractQueryImpl of(ContractDb db) {
    return new ContractQueryImpl(Uni.createFrom().item(db));
  }
  
  
  public static List<ContractContainer> groupByContract(ContractDbQuery.World world) {
    final var builders = new java.util.HashMap<String, io.resys.thena.contract.client.api.ImmutableContractContainer.Builder>();
    
    // Initialize builders for each contract
    for(final var contract : world.getContract().values()) {
      builders.put(contract.getId(), io.resys.thena.contract.client.api.ImmutableContractContainer.builder()
          .contract(contract));
    }
    
    // Group all entities by contract ID
    world.getParty().values().forEach(party -> {
      if(builders.containsKey(party.getContractId())) {
        builders.get(party.getContractId()).addParties(party);
      }
    });
    
    world.getCoverage().values().forEach(coverage -> {
      if(builders.containsKey(coverage.getContractId())) {
        builders.get(coverage.getContractId()).addCoverages(coverage);
      }
    });
    
    world.getReference().values().forEach(reference -> {
      if(builders.containsKey(reference.getContractId())) {
        builders.get(reference.getContractId()).addReferences(reference);
      }
    });
    
    world.getNote().values().forEach(note -> {
      if(builders.containsKey(note.getContractId())) {
        builders.get(note.getContractId()).addNotes(note);
      }
    });
    
    world.getCapability().values().forEach(capability -> {
      if(builders.containsKey(capability.getContractId())) {
        builders.get(capability.getContractId()).addCapabilities(capability);
      }
    });
    
    world.getInvPlan().values().forEach(invPlan -> {
      if(builders.containsKey(invPlan.getContractId())) {
        builders.get(invPlan.getContractId()).addInvPlans(invPlan);
      }
    });
    
    world.getPaymentPlan().values().forEach(paymentPlan -> {
      if(builders.containsKey(paymentPlan.getContractId())) {
        builders.get(paymentPlan.getContractId()).addPaymentPlans(paymentPlan);
      }
    });
    
    // Group InvPlanAlloc by InvPlan ID within each contract
    final var invPlanAllocsByContract = new java.util.HashMap<String, java.util.Map<String, java.util.List<io.resys.thena.contract.client.entities.InvPlanAlloc>>>();
    
    world.getInvPlanAlloc().values().forEach(invPlanAlloc -> {
      final var contractId = invPlanAlloc.getTransitives().getContractId();
      if(builders.containsKey(contractId)) {
        invPlanAllocsByContract.computeIfAbsent(contractId, k -> new java.util.HashMap<>())
            .computeIfAbsent(invPlanAlloc.getInvPlanId(), k -> new java.util.ArrayList<>())
            .add(invPlanAlloc);
      }
    });
    
    // Set InvPlanAllocations for each contract
    invPlanAllocsByContract.forEach((contractId, allocMap) -> {
      if(builders.containsKey(contractId)) {
        builders.get(contractId).invPlanAllocations(allocMap);
      }
    });
    
    return builders.values().stream()
        .map(builder -> builder.build())
        .collect(Collectors.toList());
  }
}
