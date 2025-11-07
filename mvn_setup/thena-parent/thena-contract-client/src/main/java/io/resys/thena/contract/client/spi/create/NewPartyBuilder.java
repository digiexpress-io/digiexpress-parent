package io.resys.thena.contract.client.spi.create;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewParty;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.ContractEntity.ContractRelationType;
import io.resys.thena.contract.client.entities.ImmutableContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableParty;
import io.resys.thena.contract.client.entities.Party;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewPartyBuilder implements NewParty {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final Map<String, Party> allParties;
  private final ImmutableParty.Builder next;
  private final ImmutablePersistenceUnit currentTx;
  private final ContractContainer savedState;
  private boolean built;
  private ImmutableParty result;
  
  public NewPartyBuilder(
      ContractCommitBuilder logger, 
      String contractId,

      ImmutablePersistenceUnit currentTx,
      ContractContainer savedState) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.currentTx = currentTx;
    this.savedState = savedState;
    this.next = ImmutableParty.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId)
        .partyEffectiveTo(Optional.empty())
        .partyTermEndDate(Optional.empty())
        .partyTermEndDateInterval(Optional.empty())
        .partyTermEndDateType(Optional.empty())
        .partyData(Optional.empty());
    
    final var updates = currentTx.getPartyUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = currentTx.getPartyDeletes().stream().map(e -> e.getId()).toList();
    
    this.allParties = Stream.of(
        // from current TX
        currentTx.getPartyInserts().stream(),
        currentTx.getPartyUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getParties())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !deletes.contains(saved.getId()))
          .filter(saved -> !updates.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
    
    
  }

  @Override
  public NewParty externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewParty partyType(String partyType) {
    this.next.partyType(partyType);
    return this;
  }

  @Override
  public NewParty partyEffectiveFrom(LocalDate partyEffectiveFrom) {
    this.next.partyEffectiveFrom(partyEffectiveFrom);
    return this;
  }

  @Override
  public NewParty partyEffectiveTo(@Nullable LocalDate partyEffectiveTo) {
    this.next.partyEffectiveTo(Optional.ofNullable(partyEffectiveTo));
    return this;
  }

  @Override
  public NewParty partyTermStartDate(LocalDate partyTermStartDate) {
    this.next.partyTermStartDate(partyTermStartDate);
    return this;
  }

  @Override
  public NewParty partyTermStartDateInterval(@Nullable Period partyTermStartDateInterval) {
    this.next.partyTermStartDateInterval(partyTermStartDateInterval);
    return this;
  }

  @Override
  public NewParty partyTermStartDateType(@Nullable String partyTermStartDateType) {
    this.next.partyTermStartDateType(partyTermStartDateType);
    return this;
  }

  @Override
  public NewParty partyTermEndDate(@Nullable LocalDate partyTermEndDate) {
    this.next.partyTermEndDate(partyTermEndDate);
    return this;
  }

  @Override
  public NewParty partyTermEndDateInterval(@Nullable Period partyTermEndDateInterval) {
    this.next.partyTermEndDateInterval(partyTermEndDateInterval);
    return this;
  }

  @Override
  public NewParty partyTermEndDateType(@Nullable String partyTermEndDateType) {
    this.next.partyTermEndDateType(partyTermEndDateType);
    return this;
  }

  @Override
  public NewParty partyData(@Nullable JsonObject partyData) {
    this.next.partyData(partyData);
    return this;
  }

  @Override
  public NewParty addNote(Consumer<NewNote> note) {
    final var partyRel = ImmutableContractOneOfRelations.builder()
        .relationType(ContractRelationType.PARTY)
        .partyId(this.next.build().getId())
        .build();
    
    final var builder = new NewNoteBuilder(logger, contractId, partyRel, currentTx, savedState);
    note.accept(builder);
    final var built = builder.close();
    this.logger.add(built);
    return this;
  }

  @Override
  public NewParty addReference(Consumer<NewReference> reference) {
    final var partyRel = ImmutableContractOneOfRelations.builder()
        .relationType(ContractRelationType.PARTY)
        .partyId(this.next.build().getId())
        .build();
    
    final var builder = new NewReferenceBuilder(logger, contractId, partyRel, currentTx, savedState);
    reference.accept(builder);
    final var built = builder.close();
    this.logger.add(built);
    return this;
  }

  @Override
  public Party build() {
    this.built = true;
    result = next.build();
    
    // Validate uniqueness - no duplicate parties with same external ID
    RepoAssert.isTrue(
        this.allParties.values().stream()
        .filter(p -> p.getExternalId().equals(result.getExternalId()))
        .count() == 0
        , () -> "can't have duplicate parties with same external ID!");

    this.logger.add(result);
    return result;
  }

  public ImmutableParty close() {
    RepoAssert.isTrue(built, () -> "you must call NewParty.build() to finalize party CREATE!");
    return result;
  }
}