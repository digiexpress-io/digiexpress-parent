package io.resys.thena.contract.client.spi.create;

import java.util.Collections;

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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableNote;
import io.resys.thena.contract.client.entities.Note;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewNoteBuilder implements NewNote {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final @Nullable ContractOneOfRelations relation; // for party/coverage notes
  private final Map<String, Note> allNotes;
  private final ImmutableNote.Builder next;
  private final String noteId;
  private boolean built;
  
  public NewNoteBuilder(
      ContractCommitBuilder logger, 
      String contractId,
      ContractOneOfRelations relation,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState
  ) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.relation = relation;
    this.noteId = OidUtils.gen();
    this.next = ImmutableNote.builder()
        .id(noteId)
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId)
        .relations(relation);
    
    final var updates = currentTx.getNoteUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = currentTx.getNoteDeletes().stream().map(e -> e.getId()).toList();
    
    this.allNotes = Stream.of(
        // from current TX
        currentTx.getNoteInserts().stream(),
        currentTx.getNoteUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getNotes())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !deletes.contains(saved.getId()))
          .filter(saved -> !updates.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
    
    
  }

  @Override
  public NewNote relations(@Nullable ContractOneOfRelations relations) {
    this.next.relations(relations);
    return this;
  }

  @Override
  public NewNote noteType(String noteType) {
    this.next.noteType(noteType);
    return this;
  }

  @Override
  public NewNote noteValue(String noteValue) {
    this.next.noteValue(noteValue);
    return this;
  }

  @Override
  public NewNote noteBody(@Nullable JsonObject noteBody) {
    this.next.noteBody(noteBody);
    return this;
  }

  @Override
  public String build() {
    this.built = true;
    return noteId;
  }

  public ImmutableNote close() {
    RepoAssert.isTrue(built, () -> "you must call NewNote.build() to finalize note CREATE!");
    
    final var note = next.build();
    
    this.logger.add(note);
    
    return note;
  }
}