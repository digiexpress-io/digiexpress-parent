package io.resys.hdes.docdb.spi.diff;

import java.time.LocalDateTime;
import java.util.Optional;

import io.resys.hdes.docdb.api.models.Objects;
import io.resys.hdes.docdb.api.models.Objects.Commit;

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
    private final Objects repo;
    private final int index;
    private final Commit commit;
    private final Optional<CommitHistory> after;
    private final CommitHistorySelectBean select;
    private Optional<CommitHistory> before;
    
    public CommitHistoryBean(Objects repo, String commit) {
      this.index = 0;
      this.repo = repo;
      this.commit = (Commit) repo.getValues().get(commit);
      this.after = Optional.empty();
      this.select = new CommitHistorySelectBean().setSelect(this);
    }
    
    private CommitHistoryBean(Objects repo, Commit commit, int index, CommitHistory after, CommitHistorySelectBean select) {
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
    public CommitHistory from(Objects repo, String commit) {
      return new CommitHistoryBean(repo, commit);
    }
    public CommitHistory from(Objects repo, String commit, LocalDateTime at) {
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
