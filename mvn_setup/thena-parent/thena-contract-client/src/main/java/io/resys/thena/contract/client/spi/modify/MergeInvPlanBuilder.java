package io.resys.thena.contract.client.spi.modify;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeInvPlan;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeInvPlanAlloc;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlanAlloc;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.resys.thena.contract.client.entities.ContractEntity.ContractRelationType;
import io.resys.thena.contract.client.entities.ImmutableContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableInvPlan;
import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.contract.client.entities.Note;
import io.resys.thena.contract.client.entities.Reference;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.spi.create.NewInvPlanAllocBuilder;
import io.resys.thena.contract.client.spi.create.NewNoteBuilder;
import io.resys.thena.contract.client.spi.create.NewReferenceBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class MergeInvPlanBuilder implements MergeInvPlan {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final ImmutableContractOneOfRelations childRel;
  private final InvPlan currentInvPlan;
  private final ImmutableInvPlan.Builder nextInvPlan;
  private final ContractContainer container;
  private final String contractId;

  private boolean built;

  public MergeInvPlanBuilder(
      ContractContainer container, 
      ContractCommitBuilder logger, 
      String contractId, 
      String invPlanId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.contractId = contractId;
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentInvPlan = container.getInvPlans().stream()
        .filter(i -> i.getId().equals(invPlanId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentInvPlan, () -> "Can't find investment plan with id: '" + invPlanId + "' for contract: '" + contractId + "'!");
    this.nextInvPlan = ImmutableInvPlan.builder().from(currentInvPlan);
    this.container = container;
    this.childRel = ImmutableContractOneOfRelations.builder().relationType(ContractRelationType.INV_PLAN).invPlanId(currentInvPlan.getId()).build();
  }

  @Override
  public MergeInvPlan externalId(String externalId) {
    this.nextInvPlan.externalId(externalId);
    return this;
  }
  @Override
  public MergeInvPlan invPlanStatus(String invPlanStatus) {
    this.nextInvPlan.invPlanStatus(invPlanStatus);
    return this;
  }
  @Override
  public MergeInvPlan invPlanCode(String invPlanCode) {
    this.nextInvPlan.invPlanCode(invPlanCode);
    return this;
  }
  @Override
  public MergeInvPlan invPlanName(String invPlanName) {
    this.nextInvPlan.invPlanName(invPlanName);
    return this;
  }
  @Override
  public MergeInvPlan invPlanStartDate(LocalDate invPlanStartDate) {
    this.nextInvPlan.invPlanStartDate(invPlanStartDate);
    return this;
  }
  @Override
  public MergeInvPlan invPlanStartDateInterval(Period invPlanStartDateInterval) {
    this.nextInvPlan.invPlanStartDateInterval(invPlanStartDateInterval);
    return this;
  }
  @Override
  public MergeInvPlan invPlanStartDateType(String invPlanStartDateType) {
    this.nextInvPlan.invPlanStartDateType(invPlanStartDateType);
    return this;
  }
  @Override
  public MergeInvPlan invPlanEndDate(LocalDate invPlanEndDate) {
    this.nextInvPlan.invPlanEndDate(Optional.ofNullable(invPlanEndDate));
    return this;
  }
  @Override
  public MergeInvPlan invPlanEndDateInterval(Period invPlanEndDateInterval) {
    this.nextInvPlan.invPlanEndDateInterval(Optional.ofNullable(invPlanEndDateInterval));
    return this;
  }
  @Override
  public MergeInvPlan invPlanEndDateType(String invPlanEndDateType) {
    this.nextInvPlan.invPlanEndDateType(Optional.ofNullable(invPlanEndDateType));
    return this;
  }
  @Override
  public <T> MergeInvPlan setAllAllocations(List<T> replacements, Function<T, Consumer<NewInvPlanAlloc>> callbacks) {
    // current tx
    final List<InvPlanAlloc> saved = this.batch.build().getInvPlanAllocInserts().stream()
        .filter(e -> !e.getInvPlanId().equals(currentInvPlan.getId()))
        .toList();
    this.batch.noteInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getInvPlanAllocUpdates().stream()
      .filter(e -> e.getInvPlanId().equals(currentInvPlan.getId()))
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update allocation and then delete it by setting all allocations to new values!");
    }
    
  
    final var allAllocations = new HashMap<String, InvPlanAlloc>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllInvPlanAllocDeletes(container.getInvPlanAllocations().values().stream()
        .flatMap(e -> e.stream())
        .filter(e -> e.getInvPlanId().equals(currentInvPlan.getId()))
        .toList());
    
    // add new
    for(final var replacement : replacements) {
      final var allocation = callbacks.apply(replacement);
      
      final var builder = new NewInvPlanAllocBuilder(logger, currentInvPlan.getId(), this.batch.build(), container);
      allocation.accept(builder);
      final var built = builder.close();
      allAllocations.put(built.getId(), built);
      this.batch.addInvPlanAllocInserts(built);
    }
    return this;
  }
  @Override
  public MergeInvPlan addAllocation(Consumer<NewInvPlanAlloc> allocation) {
    final var allAllocations = this.batch.build();
    
    final var builder = new NewInvPlanAllocBuilder(logger, currentInvPlan.getId(), allAllocations, container);
    allocation.accept(builder);
    final var built = builder.close();
    this.batch.addInvPlanAllocInserts(built);
    return this;
  }
  @Override
  public MergeInvPlan modifyAllocation(String allocId, Consumer<MergeInvPlanAlloc> allocation) {
    // Find the allocation
    final var current = this.container.getInvPlanAllocations().values().stream()
        .flatMap(e -> e.stream())
        .filter(a -> a.getId().equals(allocId))
        .findFirst()
        .orElse(null);
    
    if (current == null) {
      throw new IllegalArgumentException("Cannot find allocation with id: '" + allocId + "'");
    }
    
    final var builder = new MergeInvPlanAllocBuilder(current, logger);
    allocation.accept(builder);
    final var built = builder.close();
    this.batch.addInvPlanAllocUpdates(built);
    return this;
  }
  
  @Override
  public MergeInvPlan removeAllocation(String allocId) {
    final var current = this.container.getInvPlanAllocations().values().stream()
        .flatMap(e -> e.stream())
        .filter(a -> a.getId().equals(allocId))
        .findFirst()
        .orElse(null);
    
    if (current == null) {
      throw new IllegalArgumentException("Cannot find allocation with id: '" + allocId + "'");
    }
    this.batch.addInvPlanAllocDeletes(current);
    return this;
  }
  
  @Override
  public <T> MergeInvPlan setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note) {
    // current tx
    final List<Note> saved = this.batch.build().getNoteInserts().stream()
        .filter(e -> !e.getNoteType().equals(noteType))
        .filter(a -> !isInvPlanRelation(a.getRelations()))
        .toList();
    this.batch.noteInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getNoteUpdates().stream()
      .filter(e -> e.getNoteType().equals(noteType))
      .filter(a -> isInvPlanRelation(a.getRelations()))
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update note and then delete it by setting all investment plan notes to new values!");
    }
    
  
    final var all_notes = new HashMap<String, Note>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllNoteDeletes(container.getNotes().stream()
        .filter(a -> isInvPlanRelation(a.getRelations()))
        .filter(e -> e.getNoteType().equals(noteType))
        .toList());
    
    // add new
    for(final var replacement : replacements) {
      final var new_note = note.apply(replacement);
      
      final var builder = new NewNoteBuilder(logger, contractId, childRel, batch.build(), container);
      new_note.accept(builder);

      final var built = builder.close();
      all_notes.put(built.getId(), built);
      this.batch.addNoteInserts(built);
    }
    return this;
  }
  @Override
  public <T> MergeInvPlan setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> reference) {
    // current tx
    final List<Reference> saved = this.batch.build().getReferenceInserts().stream()
        .filter(e -> !e.getReferenceType().equals(referenceType))
        .filter(a -> !isInvPlanRelation(a.getRelations()))
        .toList();
    this.batch.noteInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getReferenceUpdates().stream()
      .filter(e -> e.getReferenceType().equals(referenceType))
      .filter(a -> isInvPlanRelation(a.getRelations()))
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update reference and then delete it by setting all investment plan references to new values!");
    }
    
  
    final var all_references = new HashMap<String, Reference>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllReferenceDeletes(container.getReferences().stream()
        .filter(a -> isInvPlanRelation(a.getRelations()))
        .filter(e -> e.getReferenceType().equals(referenceType))
        .toList());
    
    // add new
    for(final var replacement : replacements) {
      final var new_note = reference.apply(replacement);
      
      final var builder = new NewReferenceBuilder(logger, contractId, childRel, batch.build(), container);
      new_note.accept(builder);

      final var built = builder.close();
      all_references.put(built.getId(), built);
      this.batch.addReferenceInserts(built);
    }
    return this;
  }
  
  @Override
  public MergeInvPlan addReference(Consumer<NewReference> reference) {
    final var builder = new NewReferenceBuilder(
        logger, contractId, childRel,
        batch.build(),
        container
    );
    reference.accept(builder);
    final var built = builder.close();
    this.batch.addReferenceInserts(built);
    return this;
  }

  @Override
  public MergeInvPlan addNote(Consumer<NewNote> note) {
    final var builder = new NewNoteBuilder(
        logger, contractId, childRel,
        batch.build(),
        container
    );
    note.accept(builder);
    final var built = builder.close();
    this.batch.addNoteInserts(built);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeInvPlan.build() to finalize investment plan MERGE!");
    
    var nextInvPlan = this.nextInvPlan.build();
    final var isModified = !nextInvPlan.equals(currentInvPlan);
    if(isModified) {
      nextInvPlan = ImmutableInvPlan.builder()
          .from(nextInvPlan)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentInvPlan, nextInvPlan);
      batch.addInvPlanUpdates(nextInvPlan);
    }
    return batch.build();
  }
  
  private boolean isInvPlanRelation(ContractOneOfRelations rel) {
    if(rel == null) {
      return false;
    }
    return rel.getRelationType() == ContractRelationType.INV_PLAN && 
        rel.getInvPlanId().equals(this.currentInvPlan.getId());
  }
}