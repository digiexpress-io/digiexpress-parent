package io.resys.thena.spi;

import io.resys.thena.api.models.Repo;
import io.resys.thena.api.models.ThenaDocObject.Doc;
import io.resys.thena.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.api.models.ThenaDocObject.DocFlatted;
import io.resys.thena.api.models.ThenaDocObject.DocLog;
import io.resys.thena.api.models.ThenaGitObject.Blob;
import io.resys.thena.api.models.ThenaGitObject.BlobHistory;
import io.resys.thena.api.models.ThenaGitObject.Branch;
import io.resys.thena.api.models.ThenaGitObject.Commit;
import io.resys.thena.api.models.ThenaGitObject.CommitTree;
import io.resys.thena.api.models.ThenaGitObject.Tag;
import io.resys.thena.api.models.ThenaGitObject.Tree;
import io.resys.thena.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.structures.git.GitInserts.BatchStatus;
import io.vertx.mutiny.sqlclient.Row;

public interface DataMapper<T> {
  Repo repo(T row);
  Commit commit(T row);
  Tree tree(T row);
  TreeValue treeItem(T row);
  Tag tag(T row);
  Branch ref(T row);
  Blob blob(T row);
  BlobHistory blobHistory(T row);
  CommitTree commitTree(T row);
  CommitTree commitTreeWithBlobs(Row row);
  
  Doc doc(T row);
  DocFlatted docFlatted(T row);
  DocLog docLog(T row);
  DocBranch docBranch(T row);
  DocCommit docCommit(T row);
  DocBranchLock docBranchLock(T row);
  
  
  static Repo.CommitResultStatus mapStatus(BatchStatus src) {
    if(src == BatchStatus.OK) {
      return Repo.CommitResultStatus.OK;
    } else if(src == BatchStatus.CONFLICT) {
      return Repo.CommitResultStatus.CONFLICT;
    }
    return Repo.CommitResultStatus.ERROR; 
  }

}
