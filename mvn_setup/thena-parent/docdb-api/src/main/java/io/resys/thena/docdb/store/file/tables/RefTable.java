package io.resys.thena.docdb.store.file.tables;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.docdb.store.file.tables.RefTable.RefTableRow;

public interface RefTable extends Table<RefTableRow> {
  
  RefTableRow update(RefTableRow oldState, RefTableRow newState);
  
  @Value.Immutable @JsonSerialize(as = ImmutableRefTableRow.class) @JsonDeserialize(as = ImmutableRefTableRow.class)
  interface RefTableRow extends Table.Row {
    String getName();
    String getCommit();
  }


}