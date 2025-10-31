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

import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeInvPlanAlloc;
import io.resys.thena.contract.client.entities.ImmutableInvPlanAlloc;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;

public class MergeInvPlanAllocBuilder implements MergeInvPlanAlloc {

  private final InvPlanAlloc currentInvPlanAlloc;
  private final ImmutableInvPlanAlloc.Builder nextInvPlanAlloc;
  private final ContractCommitBuilder logger;
  private boolean built;

  public MergeInvPlanAllocBuilder(InvPlanAlloc currentInvPlanAlloc, ContractCommitBuilder logger) {
    this.currentInvPlanAlloc = currentInvPlanAlloc;
    this.nextInvPlanAlloc = ImmutableInvPlanAlloc.builder().from(currentInvPlanAlloc);
    this.logger = logger;
  }

  @Override
  public MergeInvPlanAlloc invPlanAllocCode(String invPlanAllocCode) {
    this.nextInvPlanAlloc.invPlanAllocCode(invPlanAllocCode);
    return this;
  }

  @Override
  public MergeInvPlanAlloc invPlanAllocName(String invPlanAllocName) {
    this.nextInvPlanAlloc.invPlanAllocName(invPlanAllocName);
    return this;
  }

  @Override
  public MergeInvPlanAlloc invPlanAllocPercentage(BigDecimal invPlanAllocPercentage) {
    this.nextInvPlanAlloc.invPlanAllocPercentage(invPlanAllocPercentage);
    return this;
  }

  @Override
  public MergeInvPlanAlloc invPlanAllocStatus(String invPlanAllocStatus) {
    this.nextInvPlanAlloc.invPlanAllocStatus(invPlanAllocStatus);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public InvPlanAlloc close() {
    if (!built) {
      throw new IllegalStateException("you must call MergeInvPlanAlloc.build() to finalize allocation MERGE!");
    }
    
    var nextInvPlanAlloc = this.nextInvPlanAlloc.build();
    final var isModified = !nextInvPlanAlloc.equals(currentInvPlanAlloc);
    if(isModified) {
      nextInvPlanAlloc = ImmutableInvPlanAlloc.builder()
          .from(nextInvPlanAlloc)
          .commitId(this.logger.getCommitId())
          .build();
    }
    return nextInvPlanAlloc;
  }
}