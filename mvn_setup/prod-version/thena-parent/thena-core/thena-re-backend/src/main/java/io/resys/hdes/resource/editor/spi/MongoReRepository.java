package io.resys.hdes.resource.editor.spi;

/*-
 * #%L
 * hdes-re-backend
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.resource.editor.api.ReRepository;
import io.resys.hdes.resource.editor.spi.support.ImmutableMongoDbConfig;
import io.resys.hdes.resource.editor.spi.support.MongoWrapper.MongoDbConfig;
import io.resys.hdes.resource.editor.spi.support.MongoWrapper.MongoTransaction;
import io.resys.hdes.resource.editor.spi.support.RepoAssert;

public class MongoReRepository implements ReRepository {
  
  private final MongoDbConfig config;
  private final MongoTransaction tx;
  
  public MongoReRepository(MongoDbConfig config, MongoTransaction tx) {
    super();
    this.config = config;
    this.tx = tx;
  }
  
  
  @Override
  public ReUpdate update() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReQuery query() {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  public static Config config() {
    return new Config();
  } 
  
  public static class Config {
    private MongoTransaction transaction;
    private MongoDbConfig config;
    private String dbName;
    
    public Config transaction(MongoTransaction transaction) {
      this.transaction = transaction;
      return this;
    }
    public Config config(MongoDbConfig config) {
      this.config = config;
      return this;
    }
    public Config dbName(String dbName) {
      this.dbName = dbName;
      return this;
    }
    public MongoReRepository build() {
      RepoAssert.notNull(transaction, () -> "transaction not defined!");
      RepoAssert.notEmpty(dbName, () -> "dbName not defined!");
      
      if (config == null) {
        config = ImmutableMongoDbConfig.builder()
            .db(dbName)
            .projects("projects")
            .build();
      }
      return new MongoReRepository(config, transaction);
    }
  }
}
