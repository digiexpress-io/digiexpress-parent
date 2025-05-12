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

import io.resys.thena.api.registry.FsRegistry;
import io.resys.thena.api.registry.fs.FsCommitRegistry;
import io.resys.thena.api.registry.fs.FsCommitTreeRegistry;
import io.resys.thena.api.registry.fs.FsDirentAssignmentRegistry;
import io.resys.thena.api.registry.fs.FsDirentDataRegistry;
import io.resys.thena.api.registry.fs.FsDirentLabelRegistry;
import io.resys.thena.api.registry.fs.FsDirentLinkRegistry;
import io.resys.thena.api.registry.fs.FsDirentRegistry;
import io.resys.thena.api.registry.fs.FsDirentRemarkRegistry;
import io.resys.thena.datasource.TenantTableNames;

public class FsRegistrySqlImpl implements FsRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames options;
  private final FsDirentAssignmentRegistry assignments;
  private final FsCommitRegistry commits;
  private final FsCommitTreeRegistry commitTrees;
  private final FsDirentDataRegistry missionData;
  private final FsDirentLabelRegistry missionLabels;
  private final FsDirentLinkRegistry missionsLinks;
  private final FsDirentRegistry mission;
  private final FsDirentRemarkRegistry remarks;
  
  public FsRegistrySqlImpl(TenantTableNames options) {
    this.options = options;
    this.commits = new FsCommitRegistrySqlImpl(options);
    this.commitTrees = new FsCommitTreeRegistrySqlImpl(options);
    this.assignments = new FsDirentAssignmentRegistrySqlImpl(options);
    this.missionData = new FsDirentDataRegistrySqlImpl(options);
    this.missionLabels = new FsDirentLabelRegistrySqlImpl(options);
    this.missionsLinks = new FsDirentLinkRegistrySqlImpl(options);
    this.mission = new FsDirentRegistrySqlImpl(options);
    this.remarks = new FsDirentRemarkRegistrySqlImpl(options);
  }

  @Override
  public FsDirentAssignmentRegistry direntAssignments() {
    return assignments;
  }
  @Override
  public FsCommitRegistry commits() {
    return commits;
  }
  @Override
  public FsCommitTreeRegistry commitTrees() {
    return commitTrees;
  }
  @Override
  public FsDirentDataRegistry direntData() {
    return missionData;
  }
  @Override
  public FsDirentLabelRegistry direntLabels() {
    return missionLabels;
  }
  @Override
  public FsDirentLinkRegistry direntLinks() {
    return missionsLinks;
  }
  @Override
  public FsDirentRegistry dirents() {
    return mission;
  }
  @Override
  public FsDirentRemarkRegistry direntRemarks() {
    return remarks;
  }
}
