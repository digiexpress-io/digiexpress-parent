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
import io.resys.thena.docdb.api.actions.DocCommitActions;
import io.resys.thena.docdb.api.actions.DocQueryActions;
import io.resys.thena.docdb.api.actions.HistoryActions;
import io.resys.thena.docdb.api.actions.OrgCommitActions;
import io.resys.thena.docdb.api.actions.OrgHistoryActions;
import io.resys.thena.docdb.api.actions.OrgQueryActions;
import io.resys.thena.docdb.api.actions.PullActions;
import io.resys.thena.docdb.api.actions.RepoActions;
import io.resys.thena.docdb.api.actions.TagActions;
import io.resys.thena.docdb.models.RepoActionsImpl;
import io.resys.thena.docdb.models.doc.actions.DocAppendActionsImpl;
import io.resys.thena.docdb.models.doc.actions.DocQueryActionsImpl;
import io.resys.thena.docdb.models.git.GitRepoQueryImpl;
import io.resys.thena.docdb.models.git.commits.CommitActionsImpl;
import io.resys.thena.docdb.models.git.diff.DiffActionsImpl;
import io.resys.thena.docdb.models.git.history.HistoryActionsDefault;
import io.resys.thena.docdb.models.git.objects.BranchActionsImpl;
import io.resys.thena.docdb.models.git.objects.ObjectsActionsImpl;
import io.resys.thena.docdb.models.git.tags.TagActionsDefault;
import io.resys.thena.docdb.models.org.actions.OrgCommitActionsImpl;
import io.resys.thena.docdb.models.org.actions.OrgHistoryActionsImpl;
import io.resys.thena.docdb.models.org.actions.OrgQueryActionsImpl;

public class DocDBDefault implements DocDB {
  private final DbState state;
  
  private RepoActions projectActions;
  private CommitActions commitActions;
  private TagActions tagActions;
  private HistoryActions historyActions;
  private PullActions pullActions;
  private DiffActions diffActions;
  private BranchActions branchActions;
  
  private DocCommitActions docAppendActions;
  private DocQueryActions docQueryActions;
  
  private OrgCommitActions orgCommitActions;
  private OrgQueryActions orgQueryActions;
  private OrgHistoryActions orgHistoryActions;
  
  public DocDBDefault(DbState state) {
    super();
    this.state = state;
  }
  
  @Override
  public RepoActions repo() {
    if(projectActions == null) {
      projectActions = new RepoActionsImpl(state); 
    }
    return projectActions;
  }
  public DbState getState() {
    return state;
  }

  @Override
  public GitModel git() {
    return new GitModel() {
      @Override
      public GitRepoQuery project() {
        return new GitRepoQueryImpl(state);
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
    return new DocModel() {
      
      @Override
      public DocQueryActions find() {
        if(docQueryActions == null) {
          docQueryActions = new DocQueryActionsImpl(state); 
        }
        return docQueryActions;
      }
      
      @Override
      public DocCommitActions commit() {
        if(docAppendActions == null) {
          docAppendActions = new DocAppendActionsImpl(state); 
        }
        return docAppendActions;
      }
    };
  }

  @Override
  public OrgModel org() {
    return new OrgModel() {
      
      @Override
      public OrgHistoryActions history() {
        if(orgHistoryActions == null) {
          orgHistoryActions = new OrgHistoryActionsImpl(state); 
        }
        return orgHistoryActions;
      }
      
      @Override
      public OrgQueryActions find() {
        if(orgQueryActions == null) {
          orgQueryActions = new OrgQueryActionsImpl(state); 
        }
        return orgQueryActions;
      }
      
      @Override
      public OrgCommitActions commit() {
        if(orgCommitActions == null) {
          orgCommitActions = new OrgCommitActionsImpl(state); 
        }
        return orgCommitActions;
      }
    };
  }
}
