package io.resys.thena.contract.client.spi;

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

import io.resys.thena.contract.client.api.ContractQueryActions;
import io.resys.thena.contract.client.spi.queries.ContractQueryImpl;
import io.resys.thena.contract.client.spi.queries.ReferenceNumberQueryImpl;
import io.resys.thena.contract.client.tables.ContractDb;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContractQueryActionsImpl implements ContractQueryActions {
  private final ContractDb startingState;
  private final String repoId;
  
  @Override
  public ContractQuery contractQuery() {
    return new ContractQueryImpl(startingState.withTenant(repoId));
  }

  @Override
  public ReferenceNumberQuery referenceNumberQuery() {
    return new ReferenceNumberQueryImpl(startingState.withTenant(repoId));
  }
}
