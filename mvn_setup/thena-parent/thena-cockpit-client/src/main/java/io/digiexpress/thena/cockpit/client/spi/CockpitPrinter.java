package io.digiexpress.thena.cockpit.client.spi;

/*-
 * #%L
 * thena-cockpit-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ComparisonChain;

import io.digiexpress.thena.cockpit.client.api.CockpitQueryActions;
import io.digiexpress.thena.cockpit.client.spi.actions.CockpitQueryActionsImpl;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import io.resys.thena.api.entities.Tenant;

public class CockpitPrinter {
  private final CockpitQueryActions queryActions;

  public CockpitPrinter(CockpitDb db) {
    super();
    this.queryActions = new CockpitQueryActionsImpl(db, null, db.getDataSource().getTenant().getId());
  }

  public String print(Tenant repo) {
    return internalPrinting(repo, false, null);
  }

  public String printWithStaticIds(Tenant repo, Map<String, String> replacements) {
    return internalPrinting(repo, true, replacements);
  }

  public String internalPrinting(Tenant repo, boolean isStatic, final Map<String, String> collector) {
    final Map<String, String> replacements = collector != null ? collector : new HashMap<>();
    
    final Function<String, String> ID = (id) -> {
      if (!isStatic) {
        return id;
      }
      if (id == null) {
        return null;
      }

      if (replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };


    StringBuilder result = new StringBuilder();

    result
        .append(System.lineSeparator())
        .append("Repo").append(System.lineSeparator())
        .append("  - id: ").append(ID.apply(repo.getId()))
        .append(", rev: ").append(ID.apply(repo.getRev())).append(System.lineSeparator())
        .append("    name: ").append(repo.getName())
        .append(", prefix: ").append(ID.apply(repo.getPrefix()))
        .append(", type: ").append(repo.getType()).append(System.lineSeparator());

    queryActions.cockpitQuery().findAll()
        .onItem()
        .transform(items -> {
          
          // Pre-process all dates and IDs for static replacement
          for (final var item : items.getObjects()) {
            final var config = item.getConfig();
            ID.apply(config.getId());
            ID.apply(config.getCommitId());
            
            // Process all related entities
            for (final var tenant : item.getTenants()) {
              ID.apply(tenant.getId());
              ID.apply(tenant.getCommitId());
              ID.apply(tenant.getCreatedCommitId());
            }
            
            for (final var props : item.getProps()) {
              ID.apply(props.getId());
              ID.apply(props.getCommitId());
              ID.apply(props.getCreatedCommitId());
            }
          }

          // Sort and print configs
          for (final var item : items.getObjects().stream()
              .sorted((a, b) -> ComparisonChain.start()
                  .compare(ID.apply(a.getConfig().getId()), ID.apply(b.getConfig().getId()))
                  .result())
              .toList()) {

            final var config = item.getConfig();
            result.append("Config: ").append(ID.apply(config.getId())).append(System.lineSeparator());

            // Print tenants
            for (final var data : item.getTenants().stream()
                .sorted((a, b) -> ComparisonChain.start()
                    .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                    .compare(a.getExternalId(), b.getExternalId())
                    .compare(a.getExternalBranch(), b.getExternalBranch())
                    .result())
                .toList()) {

              result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
            }
            
            // Print props
            for (final var data : item.getProps().stream()
                .sorted((a, b) -> ComparisonChain.start()
                    .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                    .compare(a.getCockpitConfigPropsType(), b.getCockpitConfigPropsType())
                    .result())
                .toList()) {

              result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
            }
          }

          return items;
        })
        .await().indefinitely();

    return result.toString();
  }
}
