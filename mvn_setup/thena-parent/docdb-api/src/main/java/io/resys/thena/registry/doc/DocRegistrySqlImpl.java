package io.resys.thena.registry.doc;

import io.resys.thena.api.registry.DocRegistry;
import io.resys.thena.api.registry.doc.DocBranchRegistry;
import io.resys.thena.api.registry.doc.DocCommitRegistry;
import io.resys.thena.api.registry.doc.DocLogRegistry;
import io.resys.thena.api.registry.doc.DocMainRegistry;
import io.resys.thena.datasource.TenantTableNames;

public class DocRegistrySqlImpl implements DocRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames options;
  private final DocBranchRegistry docBranches;
  private final DocCommitRegistry docCommits;
  private final DocLogRegistry docLogs;
  private final DocMainRegistry docs;
  
  public DocRegistrySqlImpl(TenantTableNames options) {
    super();
    this.options = options;
    this.docBranches = new DocBranchRegistrySqlImpl(options);
    this.docCommits = new DocCommitRegistrySqlImpl(options);
    this.docLogs = new DocLogRegistrySqlImpl(options);
    this.docs = new DocMainRegistrySqlImpl(options);
  }
  @Override
  public DocBranchRegistry docBranches() {
    return docBranches;
  }
  @Override
  public DocCommitRegistry docCommits() {
    return docCommits;
  }
  @Override
  public DocLogRegistry docLogs() {
    return docLogs;
  }
  @Override
  public DocMainRegistry docs() {
    return docs;
  }

}
