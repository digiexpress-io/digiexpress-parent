package io.resys.thena.docdb.models.org;

public interface OrgQueries {
  
  /*
  DocQuery docs();
  DocBranchQuery branches();
  DocCommitQuery commits();
  DocLogQuery logs();  
  
  interface DocQuery {
    Multi<DocFlatted> findAllFlatted(FlattedCriteria criteria);
    Multi<DocFlatted> findAllFlatted();
    Multi<Doc> findAll();
    Uni<Doc> getById(String id);
  }
  
  interface DocCommitQuery {
    Uni<DocCommit> getById(String commitId);
    Multi<DocCommit> findAll();
  }
  
  interface DocBranchQuery {
    Multi<DocBranch> findAll();
    
    Uni<DocBranchLock> getBranchLock(DocBranchLockCriteria criteria);
    Uni<List<DocBranchLock>> getBranchLocks(List<DocBranchLockCriteria> criteria);
    
    Uni<DocLock> getDocLock(DocLockCriteria criteria);
    Uni<List<DocLock>> getDocLocks(List<DocLockCriteria> criteria);
    
    Uni<DocBranch> getById(String branchId);
  }

  interface DocLogQuery {
    Multi<DocLog> findAll();
    Uni<DocLog> getById(String logId);
  }

  @Value.Immutable
  interface FlattedCriteria {
    List<String> getMatchId();
    @Nullable String getBranchName();
    @Nullable String getDocType();
    
    @Nullable Boolean getMatchOwners();
    boolean getChildren();
    boolean getOnlyActiveDocs();
  }

  @Value.Immutable
  interface DocBranchLockCriteria {
    String getBranchName();
    String getDocId();
  }
  @Value.Immutable
  interface DocLockCriteria {
    String getDocId();
  }
  */
  
}
