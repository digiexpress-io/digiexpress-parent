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

import java.util.Map;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeNote;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableNote;
import io.resys.thena.contract.client.entities.Note;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class MergeNoteBuilder implements MergeNote {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final Note currentNote; 
  private final ImmutableNote.Builder nextNote;
  private final Map<String, Note> allNotes;
  private boolean built;

  public MergeNoteBuilder(
      ContractContainer container, ContractCommitBuilder logger, 
      String contractId, String noteId, 
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentNote = container.getNotes().stream()
        .filter(n -> n.getId().equals(noteId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentNote, () -> "Can't find note with id: '" + noteId + "' for contract: '" + contractId + "'!");
    this.nextNote = ImmutableNote.builder().from(currentNote);
    this.allNotes = allNotes;
  }

  @Override
  public MergeNote noteValue(String noteValue) {
    this.nextNote.noteValue(noteValue);
    return this;
  }

  @Override
  public MergeNote noteType(String noteType) {
    this.nextNote.noteType(noteType);
    return this;
  }

  @Override
  public MergeNote noteBody(JsonObject noteBody) {
    this.nextNote.noteBody(noteBody);
    return this;
  }

  @Override
  public MergeNote relations(ContractOneOfRelations relations) {
    this.nextNote.relations(relations);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeNote.build() to finalize note MERGE!");
    
    var nextNote = this.nextNote.build();
    final var isModified = !nextNote.equals(currentNote);
    if(isModified) {
      nextNote = ImmutableNote.builder()
          .from(nextNote)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentNote, nextNote);
      batch.addNoteUpdates(nextNote);
    }
    return batch.build();
  }
}