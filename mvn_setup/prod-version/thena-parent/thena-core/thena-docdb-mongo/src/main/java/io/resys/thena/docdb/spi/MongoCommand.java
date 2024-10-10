package io.resys.thena.docdb.spi;

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


import java.util.function.Function;

import org.immutables.value.Value;

import com.mongodb.client.MongoClient;

@FunctionalInterface
public interface MongoCommand<T> {
  T accept(Function<MongoClient, T> client);
  
  
  @Value.Immutable
  interface MongoDbConfig {
    String getDb();
    String getRefs();
    String getTags();
    String getObjects();
  }
}
