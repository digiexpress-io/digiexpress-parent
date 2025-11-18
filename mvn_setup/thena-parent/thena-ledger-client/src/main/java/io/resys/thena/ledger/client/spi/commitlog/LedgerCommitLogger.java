package io.resys.thena.ledger.client.spi.commitlog;

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

import io.resys.thena.ledger.client.entities.Commit;
import io.resys.thena.ledger.client.entities.LedgerEntity;

public class LedgerCommitLogger {
  public final static List<String> SKIP = List.of("createdCommit", "updatedCommit", "commitId", "transitives");
  
  private final StringBuilder log = new StringBuilder();
  private final String tenantId;
  private final Commit commit;
  
  public LedgerCommitLogger(String tenantId, Commit commit) {
    super();
    this.tenantId = tenantId;
    this.commit = commit;
    this.log.append("Ledger commit for tenant: ").append(tenantId).append(System.lineSeparator());
  }
  
  public LedgerCommitLogger add(LedgerEntity entity) {
    log.append("  + ADD ").append(entity.getDocType()).append(": ").append(entity.getId()).append(System.lineSeparator());
    return this;
  }
  
  public LedgerCommitLogger merge(LedgerEntity previous, LedgerEntity next) {
    log.append("  * MERGE ").append(next.getDocType()).append(": ").append(next.getId()).append(System.lineSeparator());
    return this;
  }
  
  public LedgerCommitLogger remove(LedgerEntity entity) {
    log.append("  - REMOVE ").append(entity.getDocType()).append(": ").append(entity.getId()).append(System.lineSeparator());
    return this;
  }
  
  public String close() {
    return log.toString();
  }
}