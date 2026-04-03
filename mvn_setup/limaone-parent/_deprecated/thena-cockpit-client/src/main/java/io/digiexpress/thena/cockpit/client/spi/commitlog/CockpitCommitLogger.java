package io.digiexpress.thena.cockpit.client.spi.commitlog;

/*-
 * #%L
 * thena-cockpit-client
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

import java.util.Set;

import io.digiexpress.thena.cockpit.client.api.entities.CockpitCommit;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitEntity;

public class CockpitCommitLogger {
  public static final Set<String> SKIP = Set.of("transitives");
  
  private final StringBuilder log = new StringBuilder();
  private final String tenantId;
  private final CockpitCommit commit;
  
  public CockpitCommitLogger(String tenantId, CockpitCommit commit) {
    super();
    this.tenantId = tenantId;
    this.commit = commit;
  }
  
  public void add(CockpitEntity entity) {
    log
      .append("ADD: ").append(entity.getDocType()).append(" / ").append(entity.getId())
      .append(System.lineSeparator());
  }
  
  public void merge(CockpitEntity previous, CockpitEntity next) {
    log
      .append("MERGE: ").append(next.getDocType()).append(" / ").append(next.getId())
      .append(System.lineSeparator());
  }
  
  public void remove(CockpitEntity entity) {
    log
      .append("REMOVE: ").append(entity.getDocType()).append(" / ").append(entity.getId())
      .append(System.lineSeparator());
  }
  
  public String build() {
    return log.toString();
  }
}
