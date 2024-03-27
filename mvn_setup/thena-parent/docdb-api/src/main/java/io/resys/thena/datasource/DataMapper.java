package io.resys.thena.datasource;

import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocBranchLock;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocFlatted;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.BlobHistory;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.CommitTree;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
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
  
  static CommitResultStatus mapStatus(BatchStatus src) {
    if(src == BatchStatus.OK) {
      return CommitResultStatus.OK;
    } else if(src == BatchStatus.CONFLICT) {
      return CommitResultStatus.CONFLICT;
    }
    return CommitResultStatus.ERROR; 
  }

}
