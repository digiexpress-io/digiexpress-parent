package io.resys.thena.contract.client.spi.modify;

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

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeParty;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.resys.thena.contract.client.entities.ContractEntity.ContractRelationType;
import io.resys.thena.contract.client.entities.ImmutableContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableParty;
import io.resys.thena.contract.client.entities.Note;
import io.resys.thena.contract.client.entities.Party;
import io.resys.thena.contract.client.entities.Reference;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.spi.create.NewNoteBuilder;
import io.resys.thena.contract.client.spi.create.NewReferenceBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class MergePartyBuilder implements MergeParty {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final ImmutableContractOneOfRelations childRel;
  private final Party currentParty; 
  private final ImmutableParty.Builder nextParty;
  private final ContractContainer container;
  private final String contractId;

  private boolean built;

  public MergePartyBuilder(
      ContractContainer container, 
      ContractCommitBuilder logger, 
      String contractId, 
      String partyId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.contractId = contractId;
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentParty = container.getParties().stream()
        .filter(p -> p.getId().equals(partyId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentParty, () -> "Can't find party with id: '" + partyId + "' for contract: '" + contractId + "'!");
    this.nextParty = ImmutableParty.builder().from(currentParty);
    this.container = container;
    this.childRel = ImmutableContractOneOfRelations.builder().relationType(ContractRelationType.PARTY).partyId(currentParty.getId()).build();
  }

  @Override
  public MergeParty partyType(String partyType) {
    this.nextParty.partyType(partyType);
    return this;
  }

  @Override
  public MergeParty externalId(String externalId) {
    this.nextParty.externalId(externalId);
    return this;
  }

  @Override
  public MergeParty partyEffectiveFrom(LocalDate partyEffectiveFrom) {
    this.nextParty.partyEffectiveFrom(partyEffectiveFrom);
    return this;
  }

  @Override
  public MergeParty partyEffectiveTo(LocalDate partyEffectiveTo) {
    this.nextParty.partyEffectiveTo(Optional.ofNullable(partyEffectiveTo));
    return this;
  }

  @Override
  public MergeParty partyTermStartDate(LocalDate partyTermStartDate) {
    this.nextParty.partyTermStartDate(partyTermStartDate);
    return this;
  }


  @Override
  public MergeParty partyTermEndDate(LocalDate partyTermEndDate) {
    this.nextParty.partyTermEndDate(Optional.ofNullable(partyTermEndDate));
    return this;
  }


  @Override
  public MergeParty partyData(JsonObject partyData) {
    this.nextParty.partyData(Optional.ofNullable(partyData));
    return this;
  }
  
  @Override
  public <T> MergeParty setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note) {
    // current tx
    final List<Note> saved = this.batch.build().getNoteInserts().stream()
        .filter(e -> !e.getNoteType().equals(noteType))
        .filter(a -> !isPartyRelation(a.getRelations()))
        .toList();
    this.batch.noteInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getNoteUpdates().stream()
      .filter(e -> e.getNoteType().equals(noteType))
      .filter(a -> isPartyRelation(a.getRelations()))
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update note and then delete it by setting all party notes to new values!");
    }
    
  
    final var all_notes = new HashMap<String, Note>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllNoteDeletes(container.getNotes().stream()
        .filter(a -> isPartyRelation(a.getRelations()))
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
  public <T> MergeParty setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> reference) {
    // current tx
    final List<Reference> saved = this.batch.build().getReferenceInserts().stream()
        .filter(e -> !e.getReferenceType().equals(referenceType))
        .filter(a -> !isPartyRelation(a.getRelations()))
        .toList();
    this.batch.noteInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getReferenceUpdates().stream()
      .filter(e -> e.getReferenceType().equals(referenceType))
      .filter(a -> isPartyRelation(a.getRelations()))
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update reference and then delete it by setting all party references to new values!");
    }
    
  
    final var all_references = new HashMap<String, Reference>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllReferenceDeletes(container.getReferences().stream()
        .filter(a -> isPartyRelation(a.getRelations()))
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
  public MergeParty addReference(Consumer<NewReference> reference) {
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
  public MergeParty addNote(Consumer<NewNote> note) {
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
    RepoAssert.isTrue(built, () -> "you must call MergeParty.build() to finalize party MERGE!");
    
    var nextParty = this.nextParty.build();
    final var isModified = !nextParty.equals(currentParty);
    if(isModified) {
      nextParty = ImmutableParty.builder()
          .from(nextParty)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentParty, nextParty);
      batch.addPartyUpdates(nextParty);
    }
    return batch.build();
  }
  
  private boolean isPartyRelation(ContractOneOfRelations rel) {
    if(rel == null) {
      return false;
    }
    return rel.getRelationType() == ContractRelationType.PARTY && 
        rel.getPartyId().equals(this.currentParty.getId());
  }
}
