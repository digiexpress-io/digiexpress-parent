package io.resys.thena.contract.client.spi.modify;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeCapability;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeContract;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeCoverage;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeInvPlan;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeNote;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeParty;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergePaymentPlan;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeReference;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCapability;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCoverage;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewParty;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewPaymentPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.Capability;
import io.resys.thena.contract.client.entities.Coverage;
import io.resys.thena.contract.client.entities.ImmutableContract;
import io.resys.thena.contract.client.entities.ImmutableContractTransitives;
import io.resys.thena.contract.client.entities.Note;
import io.resys.thena.contract.client.entities.Party;
import io.resys.thena.contract.client.entities.PaymentPlan;
import io.resys.thena.contract.client.entities.Reference;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.spi.create.NewCapabilityBuilder;
import io.resys.thena.contract.client.spi.create.NewCoverageBuilder;
import io.resys.thena.contract.client.spi.create.NewInvPlanBuilder;
import io.resys.thena.contract.client.spi.create.NewNoteBuilder;
import io.resys.thena.contract.client.spi.create.NewPartyBuilder;
import io.resys.thena.contract.client.spi.create.NewPaymentPlanBuilder;
import io.resys.thena.contract.client.spi.create.NewReferenceBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;

public class MergeContractBuilder implements MergeContract {
  private final ContractContainer container;
  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final ImmutableContract.Builder nextContract;
  private final ImmutableContractTransitives.Builder nextTransitives;
  private final String contractId;
  private Consumer<ContractContainer> handleCurrentState;
  private boolean built;
  
  public MergeContractBuilder(ContractContainer container, ContractCommitBuilder logger) {
    super();

    final var start = container.getContract();
    this.nextTransitives = ImmutableContractTransitives.builder()
        .from(start.getTransitives());
    
    this.container = container;
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.nextContract = ImmutableContract.builder().from(start);
    this.contractId = container.getContract().getId();
  }
  
  @Override
  public MergeContract onCurrentState(Consumer<ContractContainer> handleCurrentState) {
    this.handleCurrentState = handleCurrentState;
    return this;
  }
  
  @Override
  public ContractContainer getCurrentState() {
    return container;
  }
  
  @Override
  public MergeContract contractNumber(String contractNumber) {
    this.nextContract.contractNumber(contractNumber);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract parentContractId(String parentContractId) {
    RepoAssert.isTrue(parentContractId == null || !parentContractId.equals(contractId), () -> "parent contract id can't be itself!");
    this.nextContract.parentContractId(parentContractId);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract externalId(String externalId) {
    this.nextContract.externalId(externalId);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractIssueDate(LocalDate contractIssueDate) {
    this.nextContract.contractIssueDate(contractIssueDate);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractIssueDateInterval(Duration contractIssueDateInterval) {
    this.nextContract.contractIssueDateInterval(contractIssueDateInterval);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractIssueDateType(String contractIssueDateType) {
    this.nextContract.contractIssueDateType(contractIssueDateType);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractStartDate(LocalDate contractStartDate) {
    this.nextContract.contractStartDate(contractStartDate);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractStartDateInterval(Duration contractStartDateInterval) {
    this.nextContract.contractStartDateInterval(contractStartDateInterval);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractStartDateType(String contractStartDateType) {
    this.nextContract.contractStartDateType(contractStartDateType);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractMaturityDate(LocalDate contractMaturityDate) {
    this.nextContract.contractMaturityDate(contractMaturityDate);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractMaturityDateInterval(Duration contractMaturityDateInterval) {
    this.nextContract.contractMaturityDateInterval(contractMaturityDateInterval);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractMaturityDateType(String contractMaturityDateType) {
    this.nextContract.contractMaturityDateType(contractMaturityDateType);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractStatus(String contractStatus) {
    this.nextContract.contractStatus(contractStatus);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractSubStatus(String contractSubStatus) {
    this.nextContract.contractSubStatus(contractSubStatus);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractType(String contractType) {
    this.nextContract.contractType(contractType);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractSubType(String contractSubType) {
    this.nextContract.contractSubType(contractSubType);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract contractData(JsonObject contractData) {
    this.nextContract.contractData(contractData);
    updateVersion();
    return this;
  }
  
  @Override
  public <T> MergeContract setAllParties(String partyType, List<T> replacements, Function<T, Consumer<NewParty>> callbacks) {
    // current tx
    final List<Party> saved = this.batch.build().getPartyInserts().stream()
        .filter(e -> !e.getPartyType().equals(partyType))
        .toList();
    this.batch.partyInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getPartyUpdates().stream()
      .filter(e -> e.getPartyType().equals(partyType))
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update party and then delete it by setting all contract parties to new values!");
    }
    
  
    final var all_parties = new HashMap<String, Party>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllPartyDeletes(container.getParties().stream()
        .filter(e -> e.getPartyType().equals(partyType))
        .toList());
    
    // add new
    for(final var replacement : replacements) {
      final var new_party = callbacks.apply(replacement);
      
      final var builder = new NewPartyBuilder(logger, contractId, batch.build(), container);
      new_party.accept(builder);

      final var built = builder.close();
      all_parties.put(built.getId(), built);
      this.batch.addPartyInserts(built);
    }
    updateVersion();
    return this;
  }
  
  // Add other setAll methods following same pattern...
  @Override
  public <T> MergeContract setAllCoverages(String coverageType, List<T> replacements, Function<T, Consumer<NewCoverage>> callbacks) {
    // current tx
    final List<Coverage> saved = this.batch.build().getCoverageInserts().stream()
        .filter(e -> !e.getCoverageType().equals(coverageType))
        .toList();
    this.batch.coverageInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getCoverageUpdates().stream()
      .filter(e -> e.getCoverageType().equals(coverageType))
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update coverage and then delete it by setting all contract coverages to new values!");
    }
    
  
    final var all_coverages = new HashMap<String, Coverage>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllCoverageDeletes(container.getCoverages().stream()
        .filter(e -> e.getCoverageType().equals(coverageType))
        .toList());
    
    // add new
    for(final var replacement : replacements) {
      final var new_coverage = callbacks.apply(replacement);
      
      final var builder = new NewCoverageBuilder(logger, contractId, batch.build(), container);
      new_coverage.accept(builder);

      final var built = builder.close();
      all_coverages.put(built.getId(), built);
      this.batch.addCoverageInserts(built);
    }
    updateVersion();
    return this;
  }
  
  @Override
  public <T> MergeContract setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> callbacks) {
    // current tx
    final List<Reference> saved = this.batch.build().getReferenceInserts().stream()
        .filter(e -> !e.getReferenceType().equals(referenceType))
        .filter(a -> a.getRelations() != null)
        .toList();
    this.batch.referenceInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getReferenceUpdates().stream()
      .filter(e -> e.getReferenceType().equals(referenceType))
      .filter(a -> a.getRelations() == null)
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update reference and then delete it by setting all contract references to new values!");
    }
    
  
    final var all_references = new HashMap<String, Reference>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllReferenceDeletes(container.getReferences().stream()
        .filter(a -> a.getRelations() == null)
        .filter(e -> e.getReferenceType().equals(referenceType))
        .toList());
    
    // add new
    for(final var replacement : replacements) {
      final var new_reference = callbacks.apply(replacement);
      
      final var builder = new NewReferenceBuilder(logger, contractId, null, batch.build(), container);
      new_reference.accept(builder);

      final var built = builder.close();
      all_references.put(built.getId(), built);
      this.batch.addReferenceInserts(built);
    }
    updateVersion();
    return this;
  }
  
  @Override
  public <T> MergeContract setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note) {
    // current tx
    final List<Note> saved = this.batch.build().getNoteInserts().stream()
        .filter(e -> !e.getNoteType().equals(noteType))
        .filter(a -> a.getRelations() != null)
        .toList();
    this.batch.noteInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getNoteUpdates().stream()
      .filter(e -> e.getNoteType().equals(noteType))
      .filter(a -> a.getRelations() == null)
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update note and then delete it by setting all contract notes to new values!");
    }
    
  
    final var all_notes = new HashMap<String, Note>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllNoteDeletes(container.getNotes().stream()
        .filter(a -> a.getRelations() == null)
        .filter(e -> e.getNoteType().equals(noteType))
        .toList());
    
    // add new
    for(final var replacement : replacements) {
      final var new_note = note.apply(replacement);
      
      final var builder = new NewNoteBuilder(logger, contractId, null, batch.build(), container);
      new_note.accept(builder);

      final var built = builder.close();
      all_notes.put(built.getId(), built);
      this.batch.addNoteInserts(built);
    }
    updateVersion();
    return this;
  }
  
  @Override
  public <T> MergeContract setAllCapabilities(List<T> replacements, Function<T, Consumer<NewCapability>> callbacks) {
    // clear old
    final var allCapabilities = new HashMap<String, Capability>();
    
    // delete old
    final var toBeDeleted = new ArrayList<>(container.getCapabilities().stream()
        .map(c -> {
          logger.rm(c);
          return c;
        })
        .toList());

    // add new
    for(final var replacement : replacements) {
      final var capability = callbacks.apply(replacement);
      
      final var builder = new NewCapabilityBuilder(logger, contractId, this.batch.build(), container);
      capability.accept(builder);
      final var built = builder.close();
      
      // previous version exists and is exactly the same
      final var previous = toBeDeleted.stream()
          .filter(c -> c.getCapabilityCode().equals(built.getCapabilityCode()))
          .findFirst();
      
      if(previous.isPresent()) {
        toBeDeleted.remove(previous.get());
      } else {
        allCapabilities.put(built.getId(), built);
        this.batch.addCapabilityInserts(built);        
      }
    }
    
    this.batch.addAllCapabilityDeletes(toBeDeleted);
    updateVersion();
    return this;
  }
  
  @Override
  public <T> MergeContract setAllInvPlans(List<T> replacements, Function<T, Consumer<NewInvPlan>> callbacks) {

    final var allInvPlans = new HashMap<String, io.resys.thena.contract.client.entities.InvPlan>();
    
    // delete old
    final var toBeDeleted = new ArrayList<>(container.getInvPlans().stream()
        .map(i -> {
          logger.rm(i);
          return i;
        })
        .toList());

    // add new
    for(final var replacement : replacements) {
      final var invPlan = callbacks.apply(replacement);
      
      final var builder = new NewInvPlanBuilder(logger, contractId, batch.build(), container);
      invPlan.accept(builder);
      final var built = builder.close();
      
      final var next = built.getInvPlanInserts().get(0);
      allInvPlans.put(next.getId(), next);
      this.batch.from(built);       
    }
    
    this.batch.addAllInvPlanDeletes(toBeDeleted);
    updateVersion();
    return this;
  }
  
  @Override
  public <T> MergeContract setAllPaymentPlans(List<T> replacements, Function<T, Consumer<NewPaymentPlan>> callbacks) {
    
    // delete old
    final var toBeDeleted = new ArrayList<>(container.getPaymentPlans().stream()
        .map(p -> {
          logger.rm(p);
          return p;
        })
        .toList());

    // add new
    final var allPaymentPlans = new HashMap<String, PaymentPlan>();    
    for(final var replacement : replacements) {
      final var paymentPlan = callbacks.apply(replacement);
      
      final var builder = new NewPaymentPlanBuilder(logger, contractId, batch.build(), container);
      paymentPlan.accept(builder);
      final var built = builder.close();
      
      // previous version exists and is exactly the same
      final var previous = toBeDeleted.stream()
          .filter(p -> p.getPaymentPlanStatus().equals(built.getPaymentPlanStatus()))
          .findFirst();
      
      if(previous.isPresent()) {
        toBeDeleted.remove(previous.get());
      } else {
        allPaymentPlans.put(built.getId(), built);
        this.batch.addPaymentPlanInserts(built);        
      }
    }
    
    this.batch.addAllPaymentPlanDeletes(toBeDeleted);
    updateVersion();
    return this;
  }
  
  // Add methods - delegate to New builders
  @Override
  public MergeContract addParty(Consumer<NewParty> party) {
    final var builder = new NewPartyBuilder(logger, contractId, this.batch.build(), this.container);
    party.accept(builder);
    final var built = builder.close();
    this.batch.addPartyInserts(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract addCoverage(Consumer<NewCoverage> coverage) {
    final var builder = new NewCoverageBuilder(logger, contractId, this.batch.build(), this.container);
    coverage.accept(builder);
    final var built = builder.close();
    this.batch.addCoverageInserts(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract addReference(Consumer<NewReference> reference) {

    final var builder = new NewReferenceBuilder(logger, contractId, null, this.batch.build(), container);
    reference.accept(builder);
    final var built = builder.close();
    this.batch.addReferenceInserts(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract addNote(Consumer<NewNote> note) {

    final var builder = new NewNoteBuilder(logger, contractId, null, this.batch.build(), container);
    note.accept(builder);
    final var built = builder.close();
    this.batch.addNoteInserts(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract addCapability(Consumer<NewCapability> capability) {
    final var builder = new NewCapabilityBuilder(logger, contractId, this.batch.build(), container);
    capability.accept(builder);
    final var built = builder.close();
    this.batch.addCapabilityInserts(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract addInvPlan(Consumer<NewInvPlan> invPlan) {
    final var builder = new NewInvPlanBuilder(logger, contractId, this.batch.build(), container);
    invPlan.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract addPaymentPlan(Consumer<NewPaymentPlan> paymentPlan) {
    final var allPaymentPlans = this.batch.build();
    final var builder = new NewPaymentPlanBuilder(logger, contractId, allPaymentPlans, container);
    paymentPlan.accept(builder);
    final var built = builder.close();
    this.batch.addPaymentPlanInserts(built);
    updateVersion();
    return this;
  }
  
  // Modify methods - create merge builders for existing entities
  @Override
  public MergeContract modifyParty(String partyId, Consumer<MergeParty> party) {
    final var allParties = this.batch.build();
    final var builder = new MergePartyBuilder(container, logger, contractId, partyId, allParties, container);
    party.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract modifyCoverage(String coverageId, Consumer<MergeCoverage> coverage) {
    final var allCoverages = this.batch.build();
    final var builder = new MergeCoverageBuilder(container, logger, contractId, coverageId, allCoverages, container);
    coverage.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract modifyReference(String referenceId, Consumer<MergeReference> reference) {
    final var allReferences = this.batch.build();
    final var builder = new MergeReferenceBuilder(container, logger, contractId, referenceId, allReferences, container);
    reference.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract modifyNote(String noteId, Consumer<MergeNote> note) {
    final var allNotes = this.batch.build();
    final var builder = new MergeNoteBuilder(container, logger, contractId, noteId, allNotes, container);
    note.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract modifyCapability(String capabilityId, Consumer<MergeCapability> capability) {
    final var allCapabilities = this.batch.build();
    final var builder = new MergeCapabilityBuilder(container, logger, contractId, capabilityId, allCapabilities, container);
    capability.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract modifyInvPlan(String invPlanId, Consumer<MergeInvPlan> invPlan) {
    final var allInvPlans = this.batch.build();
    final var builder = new MergeInvPlanBuilder(container, logger, contractId, invPlanId, allInvPlans, container);
    invPlan.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract modifyPaymentPlan(String paymentPlanId, Consumer<MergePaymentPlan> paymentPlan) {
    final var allPaymentPlans = this.batch.build();
    final var builder = new MergePaymentPlanBuilder(container, logger, contractId, paymentPlanId, allPaymentPlans, container);
    paymentPlan.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    updateVersion();
    return this;
  }
  
  // Remove methods - mark for deletion
  @Override
  public MergeContract removeParty(String partyId) {
    final var toRemove = container.getParties().stream()
        .filter(p -> p.getId().equals(partyId))
        .findFirst();
    if(toRemove.isPresent()) {
      logger.rm(toRemove.get());
      this.batch.addPartyDeletes(toRemove.get());
    }
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract removeCoverage(String coverageId) {
    final var toRemove = container.getCoverages().stream()
        .filter(c -> c.getId().equals(coverageId))
        .findFirst();
    if(toRemove.isPresent()) {
      logger.rm(toRemove.get());
      this.batch.addCoverageDeletes(toRemove.get());
    }
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract removeReference(String referenceId) {
    final var toRemove = container.getReferences().stream()
        .filter(r -> r.getId().equals(referenceId))
        .findFirst();
    if(toRemove.isPresent()) {
      logger.rm(toRemove.get());
      this.batch.addReferenceDeletes(toRemove.get());
    }
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract removeNote(String noteId) {
    final var toRemove = container.getNotes().stream()
        .filter(n -> n.getId().equals(noteId))
        .findFirst();
    if(toRemove.isPresent()) {
      logger.rm(toRemove.get());
      this.batch.addNoteDeletes(toRemove.get());
    }
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract removeCapability(String capabilityId) {
    final var toRemove = container.getCapabilities().stream()
        .filter(c -> c.getId().equals(capabilityId))
        .findFirst();
    if(toRemove.isPresent()) {
      logger.rm(toRemove.get());
      this.batch.addCapabilityDeletes(toRemove.get());
    }
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract removeInvPlan(String invPlanId) {
    final var toRemove = container.getInvPlans().stream()
        .filter(i -> i.getId().equals(invPlanId))
        .findFirst();
    if(toRemove.isPresent()) {
      logger.rm(toRemove.get());
      this.batch.addInvPlanDeletes(toRemove.get());
    }
    updateVersion();
    return this;
  }
  
  @Override
  public MergeContract removePaymentPlan(String paymentPlanId) {
    final var toRemove = container.getPaymentPlans().stream()
        .filter(p -> p.getId().equals(paymentPlanId))
        .findFirst();
    if(toRemove.isPresent()) {
      logger.rm(toRemove.get());
      this.batch.addPaymentPlanDeletes(toRemove.get());
    }
    updateVersion();
    return this;
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  
  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeContract.build() to finalize contract modification!");

    final var contract = this.nextContract
        .transitives(nextTransitives
            .updatedAt(logger.getCreatedAt())
            .updatedTreeAt(logger.getCreatedAt())
            .build())
        .build();
    
    logger.add(contract);
    
    batch.addContractInserts(contract);
    final var result = batch.build();
    
    onCurrentState(result);
    
    return result;
  }
  
  private void updateVersion() {
    this.nextTransitives.updatedAt(logger.getCreatedAt());
  }
  
  private void onCurrentState(ImmutablePersistenceUnit batch) {
    if(handleCurrentState == null) {
      return;
    }
    // TODO: Build container from batch and call handleCurrentState
  }
}