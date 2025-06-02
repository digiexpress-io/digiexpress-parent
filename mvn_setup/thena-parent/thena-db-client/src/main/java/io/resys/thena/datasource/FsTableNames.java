package io.resys.thena.datasource;

/*-
 * #%L
 * thena-docdb-api
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

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;

@Value.Immutable
public abstract class FsTableNames {
  private static final FsTableNames DEFAULTS = defaults();
  
  public abstract String getPrefix();
  
  // file-system structures
  public abstract String getFsCommit();
  public abstract String getFsCommitTree();
  public abstract String getFsDirentAssignment();
  public abstract String getFsDirentData();
  public abstract String getFsDirent();
  public abstract String getFsDirentRef();
  public abstract String getFsDirentLabel();
  public abstract String getFsDirentLink();
  public abstract String getFsDirentRemark();

  
  public FsTableNames toRepo(Tenant repo) {
    final String prefix = repo.getPrefix();
    return toRepo(prefix);
  }
  
  public FsTableNames toRepo(String prefix) {
    return ImmutableFsTableNames.builder()
        .prefix(prefix)

        .fsCommit(            prefix + DEFAULTS.getFsCommit())
        .fsCommitTree(        prefix + DEFAULTS.getFsCommitTree())
        .fsDirentAssignment(  prefix + DEFAULTS.getFsDirentAssignment())
        .fsDirentData(        prefix + DEFAULTS.getFsDirentData())
        .fsDirent(            prefix + DEFAULTS.getFsDirent())
        .fsDirentRef(         prefix + DEFAULTS.getFsDirentRef())
        .fsDirentLabel(       prefix + DEFAULTS.getFsDirentLabel())
        .fsDirentLink(        prefix + DEFAULTS.getFsDirentLink())
        .fsDirentRemark(      prefix + DEFAULTS.getFsDirentRemark())
        
        .build();
  }
  
  public static FsTableNames defaults() {
    return ImmutableFsTableNames.builder()
        .prefix("")
      
        .fsCommit("fs_commits")
        .fsCommitTree("fs_commit_trees")
        .fsDirentAssignment("fs_dirent_assignment")
        .fsDirentData("fs_dirent_data")
        .fsDirent("fs_dirents")
        .fsDirentLabel("fs_dirent_labels")
        .fsDirentLink("fs_dirent_links")
        .fsDirentRemark("fs_dirent_remarks")
        .fsDirentRef("fs_dirent_ref")
        
        .build();
  }
}
