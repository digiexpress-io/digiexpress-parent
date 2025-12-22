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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeCoverage;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.resys.thena.contract.client.entities.ContractEntity.ContractRelationType;
import io.resys.thena.contract.client.entities.Coverage;
import io.resys.thena.contract.client.entities.ImmutableContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableCoverage;
import io.resys.thena.contract.client.entities.Note;
import io.resys.thena.contract.client.entities.Reference;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.spi.create.NewNoteBuilder;
import io.resys.thena.contract.client.spi.create.NewReferenceBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class MergeCoverageBuilder implements MergeCoverage {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final ImmutableContractOneOfRelations childRel;
  private final Coverage currentCoverage; 
  private final ImmutableCoverage.Builder nextCoverage;
  private final ContractContainer container;
  private final String contractId;

  private boolean built;

  public MergeCoverageBuilder(
      ContractContainer container, 
      ContractCommitBuilder logger, 
      String contractId, 
      String coverageId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.contractId = contractId;
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentCoverage = container.getCoverages().stream()
        .filter(c -> c.getId().equals(coverageId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentCoverage, () -> "Can't find coverage with id: '" + coverageId + "' for contract: '" + contractId + "'!");
    this.nextCoverage = ImmutableCoverage.builder().from(currentCoverage);
    this.container = container;
    this.childRel = ImmutableContractOneOfRelations.builder().relationType(ContractRelationType.COVERAGE).coverageId(currentCoverage.getId()).build();
  }

  @Override
  public MergeCoverage coverageType(String coverageType) {
    this.nextCoverage.coverageType(coverageType);
    return this;
  }

  @Override
  public MergeCoverage insuredId(String insuredId) {
    this.nextCoverage.insuredId(insuredId);
    return this;
  }

  @Override
  public MergeCoverage externalId(String externalId) {
    this.nextCoverage.externalId(externalId);
    return this;
  }

  @Override
  public MergeCoverage coverageCode(String coverageCode) {
    this.nextCoverage.coverageCode(coverageCode);
    return this;
  }

  @Override
  public MergeCoverage coverageSumInsured(BigDecimal coverageSumInsured) {
    this.nextCoverage.coverageSumInsured(Optional.ofNullable(coverageSumInsured));
    return this;
  }

  @Override
  public MergeCoverage coverageRate(BigDecimal coverageRate) {
    this.nextCoverage.coverageRate(Optional.ofNullable(coverageRate));
    return this;
  }

  @Override
  public MergeCoverage coverageRateType(String coverageRateType) {
    this.nextCoverage.coverageRateType(Optional.ofNullable(coverageRateType));
    return this;
  }

  @Override
  public MergeCoverage coverageStatus(String coverageStatus) {
    this.nextCoverage.coverageStatus(coverageStatus);
    return this;
  }

  @Override
  public MergeCoverage coverageEffectiveFrom(LocalDate coverageEffectiveFrom) {
    this.nextCoverage.coverageEffectiveFrom(coverageEffectiveFrom);
    return this;
  }

  @Override
  public MergeCoverage coverageEffectiveTo(LocalDate coverageEffectiveTo) {
    this.nextCoverage.coverageEffectiveTo(Optional.ofNullable(coverageEffectiveTo));
    return this;
  }

  @Override
  public MergeCoverage coverageTermStartDate(LocalDate coverageTermStartDate) {
    this.nextCoverage.coverageTermStartDate(coverageTermStartDate);
    return this;
  }


  @Override
  public MergeCoverage coverageTermEndDate(LocalDate coverageTermEndDate) {
    this.nextCoverage.coverageTermEndDate(Optional.ofNullable(coverageTermEndDate));
    return this;
  }


  @Override
  public <T> MergeCoverage setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note) {
    // current tx
    final List<Note> saved = this.batch.build().getNoteInserts().stream()
        .filter(e -> !e.getNoteType().equals(noteType))
        .filter(a -> !isCoverageRelation(a.getRelations()))
        .toList();
    this.batch.noteInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getNoteUpdates().stream()
      .filter(e -> e.getNoteType().equals(noteType))
      .filter(a -> isCoverageRelation(a.getRelations()))
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update note and then delete it by setting all coverage notes to new values!");
    }
    
  
    final var all_notes = new HashMap<String, Note>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllNoteDeletes(container.getNotes().stream()
        .filter(a -> isCoverageRelation(a.getRelations()))
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
  public <T> MergeCoverage setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> reference) {
    // current tx
    final List<Reference> saved = this.batch.build().getReferenceInserts().stream()
        .filter(e -> !e.getReferenceType().equals(referenceType))
        .filter(a -> !isCoverageRelation(a.getRelations()))
        .toList();
    this.batch.noteInserts(Collections.emptyList());
    
    
    final var incorrect_updates = this.batch.build().getReferenceUpdates().stream()
      .filter(e -> e.getReferenceType().equals(referenceType))
      .filter(a -> isCoverageRelation(a.getRelations()))
      .toList();
    if(!incorrect_updates.isEmpty()) {
      throw new IllegalModificationException("You are trying to update reference and then delete it by setting all coverage references to new values!");
    }
    
  
    final var all_references = new HashMap<String, Reference>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllReferenceDeletes(container.getReferences().stream()
        .filter(a -> isCoverageRelation(a.getRelations()))
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
  public MergeCoverage addReference(Consumer<NewReference> reference) {
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
  public MergeCoverage addNote(Consumer<NewNote> note) {
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
    RepoAssert.isTrue(built, () -> "you must call MergeCoverage.build() to finalize coverage MERGE!");
    
    var nextCoverage = this.nextCoverage.build();
    final var isModified = !nextCoverage.equals(currentCoverage);
    if(isModified) {
      nextCoverage = ImmutableCoverage.builder()
          .from(nextCoverage)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentCoverage, nextCoverage);
      batch.addCoverageUpdates(nextCoverage);
    }
    return batch.build();
  }
  
  private boolean isCoverageRelation(ContractOneOfRelations rel) {
    if(rel == null) {
      return false;
    }
    return rel.getRelationType() == ContractRelationType.COVERAGE && 
        rel.getCoverageId().equals(this.currentCoverage.getId());
  }
}