package io.resys.thena.structures.git.objects;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.PullActions.MatchCriteria;
import io.resys.thena.api.entities.git.ThenaGitObject.Blob;
import io.resys.thena.api.entities.git.ThenaGitObject.Commit;
import io.resys.thena.api.entities.git.ThenaGitObject.Tree;
import io.resys.thena.structures.git.GitState.GitRepo;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectsUtils {
  public static Uni<String> getTagCommit(String tagName, GitRepo ctx) {
    return ctx.query().tags().name(tagName).getFirst()
        .onItem().transform(tag -> tag == null ? null : tag.getCommit());
  }
  public static Uni<String> getRefCommit(String refName, GitRepo ctx) {
    return ctx.query().refs().name(refName)
        .onItem().transform(ref -> ref == null ? null : ref.getCommit());
  }
  public static Uni<Tree> getTree(Commit commit, GitRepo ctx) {
    return ctx.query().trees().getById(commit.getTree());
  }
  public static Uni<Commit> getCommit(String commit, GitRepo ctx) {
    return ctx.query().commits().getById(commit);
  }
  public static Uni<Commit> findCommit(GitRepo ctx, String anyId) {
    
    return ObjectsUtils.getTagCommit(anyId, ctx)
      .onItem().transformToUni(tag -> {
        if(tag == null) {
          log.info("Can't find from repo: '{}' a tag: '{}' trying to find by ref: '{}'", ctx.getRepoName(), anyId, anyId);
          return ObjectsUtils.getRefCommit(anyId, ctx);
        }
        return Uni.createFrom().item(tag);
      })
      .onItem().transformToUni(commitId -> {
        if(commitId == null) {
          log.info("Can't find from repo: '{}' a ref: '{}' trying to find by commit: '{}'", ctx.getRepoName(), anyId, anyId);
          return ObjectsUtils.getCommit(anyId, ctx);
        }
        return ObjectsUtils.getCommit(commitId, ctx);
      });
  }
  

  public static Uni<Map<String, Blob>> getBlobs(Tree tree, List<MatchCriteria> blobCriteria, GitRepo ctx) {
    return ctx.query().blobs().findAll(tree.getId(), blobCriteria).collect().asList().onItem()
        .transform(blobs -> blobs.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)));
  }
}
