package io.resys.thena.ledger.client.api;

/*-
 * #%L
 * thena-ledger-client
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

import java.util.List;
import java.util.Optional;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerTreeNode;
import io.resys.thena.ledger.client.entities.BlackBook;
import io.resys.thena.ledger.client.entities.ProjectionDetail;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class LedgerTreeNode_Default implements LedgerTreeNode {
  private final LedgerContainer ledger;

  @Override
  public BlackBook getBlackBook() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ProjectionDetail> getBlackBookDetails() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<LedgerTreeNode> getTill(String blackBookType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<LedgerTreeNode> getPrevious() {
    // TODO Auto-generated method stub
    return Optional.empty();
  }

  @Override
  public Optional<LedgerTreeNode> getNext() {
    // TODO Auto-generated method stub
    return Optional.empty();
  }
}
