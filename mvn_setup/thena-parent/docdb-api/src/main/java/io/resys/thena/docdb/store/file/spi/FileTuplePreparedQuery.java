package io.resys.thena.docdb.store.file.spi;

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

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.docdb.store.file.tables.Table;
import io.resys.thena.docdb.store.file.tables.Table.FileCommand;
import io.resys.thena.docdb.store.file.tables.Table.FilePreparedQuery;
import io.resys.thena.docdb.store.file.tables.Table.Row;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileTuplePreparedQuery<T> implements FilePreparedQuery<T> {
  private final File rootDir;
  private final FileCommand query;
  private final Function<Table.Row, T> mapper;
  private final ObjectMapper objectMapper;
  private final FileConnection conn;

  @Override
  public <U> FilePreparedQuery<U> mapping(Function<Table.Row, U> mapper) {
    return new FileTuplePreparedQuery<U>(rootDir, query, mapper, objectMapper, conn);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Uni<List<T>> execute() {

    return Uni.createFrom().item(() -> {
      final List<? extends Row> rows = query.getCommand().apply(conn);

      final var mapped = rows.stream().map(r -> {
        if(mapper == null) {
          return (T) r;
        } else {
          return mapper.apply(r);
        }
        
      }).collect(Collectors.toList());
      return mapped;
    });
  }
  
}
