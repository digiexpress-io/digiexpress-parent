package io.resys.hdes.client.spi.git;

/*-
 * #%L
 * eveli-integration-git
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

import io.resys.hdes.client.api.HdesStore.CommitLog;
import io.resys.hdes.client.api.HdesStore.CommitLogBuilder;
import io.resys.hdes.client.api.ImmutableCommitLog;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
public class CommitLogBuilderGitImpl implements CommitLogBuilder {
  private final Git git;

  @Override
  public Uni<List<CommitLog>> build() {
    return Uni.createFrom().item(() -> {
      try (final var reader = git.getRepository().newObjectReader();
           final var diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
           final var revWalk = new RevWalk(git.getRepository())) {
        
        final var logs = new ArrayList<CommitLog>();
        final var commits = git.log().all().call();
        diffFormatter.setRepository(git.getRepository());
        
        for (final var commit : commits) {
          revWalk.parseCommit(commit);
          
          final var newTree = new CanonicalTreeParser();
          newTree.reset(reader, commit.getTree());
          
          final var oldTree = new CanonicalTreeParser();
          if (commit.getParentCount() > 0) {
            final var parent = revWalk.parseCommit(commit.getParent(0));
            oldTree.reset(reader, parent.getTree());
          }
          
          final var diffs = commit.getParentCount() == 0
              ? diffFormatter.scan(new EmptyTreeIterator(), newTree)
              : diffFormatter.scan(oldTree, newTree);
          
          for (final var diff : diffs) {
            final var path = diff.getChangeType() == DiffEntry.ChangeType.DELETE 
                ? diff.getOldPath() 
                : diff.getNewPath();

            final var fileName = path.substring(path.lastIndexOf("/") + 1);
            final var objectId = fileName.contains(".") 
                ? fileName.substring(0, fileName.lastIndexOf(".")) 
                : fileName;

            logs.add(ImmutableCommitLog.builder()
                .commitId(commit.getName())
                .createdAt(Instant.ofEpochSecond(commit.getCommitTime()).atOffset(ZoneOffset.UTC))
                .createdBy(commit.getAuthorIdent().getName())
                .objectId(objectId)
                .build());
          }
        }
        
        return logs;
      } catch (Exception e) {
        throw new RuntimeException(String.format("Failed to build commit logs for repository %s with error: %s", 
            git.getRepository().getDirectory().getAbsolutePath(), e.getMessage()), e);
      }
    });
  }
}
