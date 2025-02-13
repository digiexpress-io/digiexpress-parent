package io.resys.thena.registry.doc;

/*-
 * #%L
 * thena-docdb-api
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

import java.util.ArrayList;
import java.util.List;

import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocsSqlFilterBuilder {
  private final TenantTableNames options;
  private final List<Object> params = new ArrayList<Object>();
  private final List<String> filters = new ArrayList<String>();
  
  public SqlTuple build() {
    final var where = (params.isEmpty() ? "" : " WHERE ") + String.join(" AND ", filters);
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement().append(where).build())
        .props(Tuple.from(params))
        .build();
  }
  
  public DocsSqlFilterBuilder docFilter(DocFilter filter) {
    if(filter.getDocIds() != null) {
      final var index = params.size() + 1;
      filters.add(" ( docs.id = ANY($" + index +") OR docs.external_id = ANY($" + index + ") OR docs.doc_name = ANY($" + index + ") ) ");
      params.add(filter.getDocIds().toArray());
    }
    
    if(filter.getDocParentId() != null) {
      final var index = params.size() + 1;
      filters.add(" ( docs.doc_parent_id = $" + index + " ) ");
      params.add(filter.getDocParentId());
    }
    
    if(filter.getDocOwnerId() != null) {
      final var index = params.size() + 1;
      filters.add(" ( docs.owner_id = $" + index + " ) ");
      params.add(filter.getDocOwnerId());
    }    
    
    if(filter.getDocType() != null) {
      final var index = params.size() + 1;
      filters.add(" ( docs.doc_type = $" + index + " ) ");
      params.add(filter.getDocType());
    }
    
    if(filter.getDocSubStatus() != null && !filter.getDocSubStatus().isEmpty()) {
      final var index = params.size() + 1;
      filters.add(" ( docs.doc_sub_status = ANY($" + index + " )) ");
      params.add(filter.getDocSubStatus().toArray());
    }
    return this;
  }
  
  
  public DocsSqlFilterBuilder branchFilter(DocFilter filter) {
    if(filter.getBranchNameOrId() != null) {
      final var index = params.size() + 1;
      filters.add(" ( branch.branch_name = $" + index + " OR branch.branch_id = $" + index + ") ");
      params.add(filter.getBranchNameOrId());
    }
    
    if(filter.getBranchValueName() != null) {
      final var index = params.size() + 1;
      filters.add(" ( branch.value_name = $" + index + ") ");
      params.add(filter.getBranchValueName());
    }
    if(filter.getBranchValueStatus() != null) {
      final var index = params.size() + 1;
      filters.add(" ( branch.value_status = $" + index + ") ");
      params.add(filter.getBranchValueStatus());
    }
    return this;
  }
  
  
  public DocsSqlFilterBuilder branchJoinFilter(DocFilter filter) {
    // Branch related filters
    final var branchValueFilter = new StringBuilder();    
    if(filter.getBranchNameOrId() != null) {
      final var index = params.size() + 1;
      branchValueFilter.append(" AND (branches.branch_name = $" + index + " OR branches.branch_id = $" + index).append(")");
      params.add(filter.getBranchNameOrId());
    }
    if(filter.getBranchValueName() != null) {
      final var index = params.size() + 1;
      branchValueFilter.append(" AND (branches.value_name = $" + index).append(")");
      params.add(filter.getBranchNameOrId());
    }
    if(filter.getBranchValueStatus() != null) {
      final var index = params.size() + 1;
      branchValueFilter.append(" AND (branches.value_status = $" + index).append(")");
      params.add(filter.getBranchNameOrId());
    }
    
    if(!branchValueFilter.isEmpty()) {
      filters.add(new StringBuilder().append("(SELECT count(branch_id) ")
          .append(" FROM ").append(options.getDocBranch()).append(" as branches ")
          .append(" WHERE branches.doc_id = docs.id ")
          .append(branchValueFilter.toString())
          .append(") > 0").toString());
    }
    

    return this;
  }
}
