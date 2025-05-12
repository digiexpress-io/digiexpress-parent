package io.resys.thena.registry.fs;

/*-
 * #%L
 * thena-db-client
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

import io.resys.thena.api.actions.FsQueryActions.FsArchiveQueryType;
import io.resys.thena.api.registry.fs.FsDirentFilter;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;


public class FsDirentSqlFilterBuilder {

  private final TenantTableNames options;
  private final List<Object> params;
  private final SqlStatement builder = new SqlStatement();
  private int index;
  
  public FsDirentSqlFilterBuilder(TenantTableNames options) {
    super();
    this.options = options;
    this.params = new ArrayList<Object>();
    this.index = 1;
  }
  
  public FsDirentSqlFilterBuilder(TenantTableNames options, List<Object> params) {
    super();
    this.options = options;
    this.params = params;
    this.index = params.size() + 1;
  }
  
  private void and() {
    if(!builder.isEmpty()) {
      builder.ln().append(" AND ");
    }
  }
  
  
  public SqlTuple where(FsDirentFilter filter) {    
    
    
    // by id
    if(filter.getDirentIds().isPresent()) {
      builder.append(" dirent.id = ANY($").append(index++).append(")").ln();
      params.add(filter.getDirentIds().get().toArray());
    }
    
    // archive filter
    if(FsArchiveQueryType.ONLY_ARCHIVED.equals(filter.getArchived())) {
      and();
      builder.append(" dirent.archived_at is NOT NULL").ln();      
    } else if(FsArchiveQueryType.ONLY_IN_FORCE.equals(filter.getArchived())) {
      and();
      builder.append(" dirent.archived_at is NULL").ln();
    }
    
    final var result = builder.toString();
    
    return ImmutableSqlTuple.builder()
        .value((result.isBlank() ? "" : " WHERE ") +builder.toString())
        .props(Tuple.from(params))
        .build();
  } 
}
