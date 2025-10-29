package io.resys.thena.contract.client.spi.create;

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

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Consumer;

import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewParty;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.ImmutableParty;
import io.resys.thena.contract.client.entities.Party;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewPartyBuilder implements NewParty {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final Map<String, Party> allParties;
  private final ImmutableParty.Builder next;
  private boolean built;
  
  public NewPartyBuilder(
      ContractCommitBuilder logger, 
      String contractId, 
      Map<String, Party> allParties) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.allParties = allParties;
    this.next = ImmutableParty.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId);
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
    this.next.partyEffectiveTo(partyEffectiveTo);
    return this;
  }

  @Override
  public NewParty partyTermStartDate(LocalDate partyTermStartDate) {
    this.next.partyTermStartDate(partyTermStartDate);
    return this;
  }

  @Override
  public NewParty partyTermStartDateInterval(@Nullable Duration partyTermStartDateInterval) {
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
  public NewParty partyTermEndDateInterval(@Nullable Duration partyTermEndDateInterval) {
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
    // TODO: Implement nested note builder
    return this;
  }

  @Override
  public NewParty addReference(Consumer<NewReference> reference) {
    // TODO: Implement nested reference builder
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableParty close() {
    RepoAssert.isTrue(built, () -> "you must call NewParty.build() to finalize party CREATE!");
    
    final var built = next.build();
    
    // Validate uniqueness - no duplicate parties with same external ID
    RepoAssert.isTrue(
        this.allParties.values().stream()
        .filter(p -> p.getExternalId().equals(built.getExternalId()))
        .count() == 0
        , () -> "can't have duplicate parties with same external ID!");

    this.logger.add(built);
    return built;
  }
}