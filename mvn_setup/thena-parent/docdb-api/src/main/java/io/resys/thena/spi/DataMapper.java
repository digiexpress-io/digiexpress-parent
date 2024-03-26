package io.resys.thena.spi;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.doc.ThenaDocObject.Doc;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranch;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranchLock;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocCommit;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocFlatted;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocLog;
import io.resys.thena.api.entities.git.ThenaGitObject.Blob;
import io.resys.thena.api.entities.git.ThenaGitObject.BlobHistory;
import io.resys.thena.api.entities.git.ThenaGitObject.Branch;
import io.resys.thena.api.entities.git.ThenaGitObject.Commit;
import io.resys.thena.api.entities.git.ThenaGitObject.CommitTree;
import io.resys.thena.api.entities.git.ThenaGitObject.Tag;
import io.resys.thena.api.entities.git.ThenaGitObject.Tree;
import io.resys.thena.api.entities.git.ThenaGitObject.TreeValue;
import io.resys.thena.structures.git.GitInserts.BatchStatus;
import io.vertx.mutiny.sqlclient.Row;

public interface DataMapper<T> {
  Tenant repo(T row);
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
  
  
  static Tenant.CommitResultStatus mapStatus(BatchStatus src) {
    if(src == BatchStatus.OK) {
      return Tenant.CommitResultStatus.OK;
    } else if(src == BatchStatus.CONFLICT) {
      return Tenant.CommitResultStatus.CONFLICT;
    }
    return Tenant.CommitResultStatus.ERROR; 
  }

}
