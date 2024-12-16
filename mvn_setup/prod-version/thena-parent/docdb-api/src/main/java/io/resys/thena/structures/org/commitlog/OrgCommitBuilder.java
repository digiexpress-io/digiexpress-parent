package io.resys.thena.structures.org.commitlog;

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

import java.util.ArrayList;
import java.util.List;

import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgCommitTree;
import io.resys.thena.api.entities.org.OrgCommit;
import io.resys.thena.api.entities.org.OrgCommitTree;
import io.resys.thena.api.entities.org.OrgCommitTree.OrgOperationType;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;



public class OrgCommitBuilder {
  private final String tenantId;
  private final String commitId;
  private final ImmutableOrgCommit.Builder commit;
  private final List<OrgCommitTree> tree;
  private final OrgCommitLogger logger;
  
  public OrgCommitBuilder(String tenantId, OrgCommit commit) {
    super();
    this.commitId = commit.getCommitId();
    this.tenantId = tenantId;
    this.tree = new ArrayList<>();
    
    this.commit = ImmutableOrgCommit.builder().from(commit);
    this.logger = new OrgCommitLogger(tenantId, commit);
  }
  public String getTenantId() {
    return tenantId;
  }
  public String getCommitId() {
    return commitId;
  }
  public OrgCommitBuilder add(IsOrgObject entity) {
    this.tree.add(ImmutableOrgCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(OrgOperationType.ADD)
        .actorId(entity.getId())
        .actorType(entity.getDocType().name())
        .bodyAfter(JsonObject.mapFrom(entity))
        .build());
    this.logger.add(entity);
    return this;
  }
  public OrgCommitBuilder merge(IsOrgObject previous, IsOrgObject next) {
    this.tree.add(ImmutableOrgCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(OrgOperationType.ADD)
        .actorId(next.getId())
        .actorType(next.getDocType().name())
        .bodyBefore(JsonObject.mapFrom(previous))
        .bodyAfter(JsonObject.mapFrom(next))
        .build());
    this.logger.merge(previous, next);
    return this;
  }
  public OrgCommitBuilder rm(IsOrgObject current) {
    this.tree.add(ImmutableOrgCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(OrgOperationType.REMOVE)
        .actorId(current.getId())
        .actorType(current.getDocType().name())
        .bodyBefore(JsonObject.mapFrom(current))
        .bodyAfter(null)
        .build());
    this.logger.remove(current);
    return this;
  }
  public Tuple2<OrgCommit, List<OrgCommitTree>> close() { 
    return Tuple2.of(this.commit.commitLog(this.logger.build()).build(), tree);
  }

}
