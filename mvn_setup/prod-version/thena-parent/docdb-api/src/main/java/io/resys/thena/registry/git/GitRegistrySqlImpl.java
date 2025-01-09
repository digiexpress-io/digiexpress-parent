package io.resys.thena.registry.git;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.registry.GitRegistry;
import io.resys.thena.api.registry.git.BlobRegistry;
import io.resys.thena.api.registry.git.BranchRegistry;
import io.resys.thena.api.registry.git.CommitRegistry;
import io.resys.thena.api.registry.git.TagRegistry;
import io.resys.thena.api.registry.git.TreeRegistry;
import io.resys.thena.api.registry.git.TreeValueRegistry;
import io.resys.thena.datasource.TenantTableNames;

public class GitRegistrySqlImpl implements GitRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames options;
  private final BlobRegistry blobs;
  private final CommitRegistry commits;
  private final TreeValueRegistry treeValues;
  private final TreeRegistry trees;
  private final BranchRegistry branches;
  private final TagRegistry tags;
  
  public GitRegistrySqlImpl(TenantTableNames options) {
    super();
    this.options = options;
    this.blobs = new BlobRegistrySqlImpl(options);
    this.commits = new CommitRegistrySqlImpl(options);
    this.treeValues = new TreeValueRegistrySqlImpl(options);
    this.trees = new TreeRegistrySqlImpl(options);
    this.branches = new BranchRegistrySqlImpl(options);
    this.tags = new TagRegistrySqlImpl(options);
  }

  @Override
  public BlobRegistry blobs() {
    return blobs;
  }
  @Override
  public CommitRegistry commits() {
    return commits;
  }
  @Override
  public TreeValueRegistry treeValues() {
    return treeValues;
  }
  @Override
  public TreeRegistry trees() {
    return trees;
  }
  @Override
  public BranchRegistry branches() {
    return branches;
  }
  @Override
  public TagRegistry tags() {
    return tags;
  }
}
