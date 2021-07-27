package io.resys.hdes.docdb.spi.mongo;

/*-
 * #%L
 * thena-docdb-mongo
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.hdes.docdb.spi.ClientQuery;

public class MongoClientQuery implements ClientQuery {
  
  private final MongoClientWrapper wrapper;
  
  public MongoClientQuery(MongoClientWrapper wrapper) {
    this.wrapper = wrapper;
  }

  @Override
  public TagQuery tags() {
    return new MongoTagQuery(wrapper);
  }

  @Override
  public CommitQuery commits() {
    return new MongoCommitQuery(wrapper);
  }

  @Override
  public RefQuery refs() {
    return new MongoRefQuery(wrapper);
  }

  @Override
  public TreeQuery trees() {
    return new MongoTreeQuery(wrapper);
  }

  @Override
  public BlobQuery blobs() {
    return new MongoBlobQuery(wrapper);
  }
}
