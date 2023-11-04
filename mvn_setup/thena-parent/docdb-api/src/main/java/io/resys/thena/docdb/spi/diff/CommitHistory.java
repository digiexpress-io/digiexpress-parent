package io.resys.thena.docdb.spi.diff;

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

import java.time.LocalDateTime;
import java.util.Optional;

import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObjects.ProjectObjects;

public interface CommitHistory {
  int getIndex();
  Commit getCommit();
  Optional<CommitHistory> getBefore();
  Optional<CommitHistory> getAfter();
  CommitHistory getSelect();
  CommitHistory setSelect();

  public static class CommitHistorySelectBean {
    private CommitHistoryBean select;

    public CommitHistoryBean getSelect() {
      return select;
    }

    public CommitHistorySelectBean setSelect(CommitHistoryBean select) {
      this.select = select;
      return this;
    }
  }
  
  public static class CommitHistoryBean implements CommitHistory {
    private final ProjectObjects repo;
    private final int index;
    private final Commit commit;
    private final Optional<CommitHistory> after;
    private final CommitHistorySelectBean select;
    private Optional<CommitHistory> before;
    
    public CommitHistoryBean(ProjectObjects repo, String commit) {
      this.index = 0;
      this.repo = repo;
      this.commit = (Commit) repo.getValues().get(commit);
      this.after = Optional.empty();
      this.select = new CommitHistorySelectBean().setSelect(this);
    }
    
    private CommitHistoryBean(ProjectObjects repo, Commit commit, int index, CommitHistory after, CommitHistorySelectBean select) {
      this.index = index;
      this.repo = repo;
      this.commit = commit;
      this.after = Optional.of(after);
      this.select = select;
    }
    @Override
    public int getIndex() {
      return index;
    }
    @Override
    public Commit getCommit() {
      return this.commit;
    }
    @Override
    public Optional<CommitHistory> getBefore() {
      if(before != null) {
        return before;
      }
      if(commit.getParent().isEmpty()) {
        before = Optional.empty();
        return before;
      }
      Commit before = (Commit) repo.getValues().get(commit.getParent().get());
      this.before = Optional.of(new CommitHistoryBean(repo, before, index + 1, this, this.select));
      return this.before;
    }
    @Override
    public Optional<CommitHistory> getAfter() {
      return after;
    }
    @Override
    public CommitHistory setSelect() {
      this.select.setSelect(this);
      return this;
    }
    @Override
    public CommitHistory getSelect() {
      this.select.getSelect();
      return this;
    }
  }
  
  public static class Builder {
    public CommitHistory from(ProjectObjects repo, String commit) {
      return new CommitHistoryBean(repo, commit);
    }
    public CommitHistory from(ProjectObjects repo, String commit, LocalDateTime at) {
      CommitHistory history = new CommitHistoryBean(repo, commit);
      
      var start = history;
      while(start.getCommit().getDateTime().isAfter(at)) {
        start = start.getBefore().get();
      }
      return start;
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
