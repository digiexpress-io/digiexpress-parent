package io.resys.thena.docdb.spi.commits;

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

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.commits.CommitVisitor.CommitOutput;
import io.resys.thena.docdb.spi.commits.CommitVisitor.CommitOutputStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommitSaveVisitor {  
  private final ClientRepoState state;

  public Uni<CommitOutput> visit(CommitOutput output) {
    // check for consistency
    return state.query().refs().nameOrCommit(output.getRef().getName())
        .onItem().transformToUni(item -> {
          
          // Create new head
          if(item == null && output.getCommit().getParent().isEmpty()) {
            return state.insert().output(output);
          }
          // Update head
          if(item != null && output.getCommit().getParent().isPresent() &&
              item.getCommit().equals(output.getCommit().getParent().get())) {
            return state.insert().output(output);
          }
          
          StringBuilder error = new StringBuilder();
          if(item == null && output.getCommit().getParent().isPresent()) {
            error.append("Commit points to unknown head: ")
              .append("'")
              .append(output.getRef().getName())
              .append("@")
              .append(output.getCommit().getParent())
              .append("'!");
          } else if(item != null && !item.getCommit().equals(output.getCommit().getParent().get())) {
            error.append("Commit is behind of the head: ")
              .append("'")
              .append(item.getName())
              .append("@")
              .append(item.getCommit())
              .append("'!");            
          }
          
          return Uni.createFrom().item((CommitOutput) ImmutableCommitOutput.builder()
            .from(output)
            .status(CommitOutputStatus.CONFLICT)
            .addMessages(ImmutableMessage.builder().text(error.toString()).build())
            .build());
        });
  }
}
