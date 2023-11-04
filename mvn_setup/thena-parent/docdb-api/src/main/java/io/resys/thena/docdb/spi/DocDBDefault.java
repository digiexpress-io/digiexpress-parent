package io.resys.thena.docdb.spi;

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

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.actions.BranchActions;
import io.resys.thena.docdb.api.actions.CommitActions;
import io.resys.thena.docdb.api.actions.DiffActions;
import io.resys.thena.docdb.api.actions.HistoryActions;
import io.resys.thena.docdb.api.actions.PullActions;
import io.resys.thena.docdb.api.actions.RepoActions;
import io.resys.thena.docdb.api.actions.TagActions;
import io.resys.thena.docdb.spi.commits.CommitActionsImpl;
import io.resys.thena.docdb.spi.diff.DiffActionsImpl;
import io.resys.thena.docdb.spi.history.HistoryActionsDefault;
import io.resys.thena.docdb.spi.objects.BranchActionsImpl;
import io.resys.thena.docdb.spi.objects.ObjectsActionsImpl;
import io.resys.thena.docdb.spi.repo.ProjectActionsImpl;
import io.resys.thena.docdb.spi.repo.RepoStateBuilderImpl;
import io.resys.thena.docdb.spi.tags.TagActionsDefault;

public class DocDBDefault implements DocDB {
  private final ClientState state;
  private RepoActions projectActions;
  private CommitActions commitActions;
  private TagActions tagActions;
  private HistoryActions historyActions;
  private PullActions pullActions;
  private DiffActions diffActions;
  private BranchActions branchActions;
  
  
  public DocDBDefault(ClientState state) {
    super();
    this.state = state;
  }
  
  @Override
  public RepoActions repo() {
    if(projectActions == null) {
      projectActions = new ProjectActionsImpl(state); 
    }
    return projectActions;
  }
  public ClientState getState() {
    return state;
  }

  @Override
  public GitModel git() {
    return new GitModel() {
      @Override
      public GitProjectQuery project() {
        return new RepoStateBuilderImpl(state);
      }
      @Override
      public CommitActions commit() {
        if(commitActions == null) {
          commitActions = new CommitActionsImpl(state); 
        }
        return commitActions;
      }
      @Override
      public TagActions tag() {
        if(tagActions == null) {
          tagActions = new TagActionsDefault(state); 
        }
        return tagActions;
      }
      @Override
      public HistoryActions history() {
        if(historyActions == null) {
          historyActions = new HistoryActionsDefault(state); 
        }
        return historyActions;
      }

      @Override
      public PullActions pull() {
        if(pullActions == null) {
          pullActions = new ObjectsActionsImpl(state); 
        }
        return pullActions;
      }

      @Override
      public DiffActions diff() {
        if(diffActions == null) {
          diffActions = new DiffActionsImpl(state, pull(), commit(), () -> project()); 
        }
        return diffActions;
      }
      @Override
      public BranchActions branch() {
        if(branchActions == null) {
          branchActions =  new BranchActionsImpl(state); 
        }
        return branchActions;
      }
    };
  }

  @Override
  public DocModel doc() {
    // TODO Auto-generated method stub
    return null;
  }
}
