package io.resys.thena.storefile.tables;

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

import io.resys.thena.storefile.tables.ImmutableTagTableRow;
import io.resys.thena.storefile.tables.TagTable.TagTableRow;

public interface TagTable extends Table<TagTableRow> {
  
  @Value.Immutable @JsonSerialize(as = ImmutableTagTableRow.class) @JsonDeserialize(as = ImmutableTagTableRow.class)
  interface TagTableRow extends Table.Row {
    String getId();
    String getDatetime();
    String getAuthor();
    String getMessage();
    String getCommit();
  }

}
