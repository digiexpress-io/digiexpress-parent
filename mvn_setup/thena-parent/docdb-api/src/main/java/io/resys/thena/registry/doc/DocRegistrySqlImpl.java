package io.resys.thena.registry.doc;

import io.resys.thena.api.registry.DocRegistry;
import io.resys.thena.api.registry.doc.DocBranchRegistry;
import io.resys.thena.api.registry.doc.DocCommandsRegistry;
import io.resys.thena.api.registry.doc.DocCommitRegistry;
import io.resys.thena.api.registry.doc.DocCommitTreeRegistry;
import io.resys.thena.api.registry.doc.DocMainRegistry;
import io.resys.thena.datasource.TenantTableNames;

public class DocRegistrySqlImpl implements DocRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames options;
  private final DocBranchRegistry docBranches;
  private final DocCommitRegistry docCommits;
  private final DocCommitTreeRegistry docLogs;
  private final DocCommandsRegistry docCommands;
  private final DocMainRegistry docs;
  
  public DocRegistrySqlImpl(TenantTableNames options) {
    super();
    this.options = options;
    this.docBranches = new DocBranchRegistrySqlImpl(options);
    this.docCommits = new DocCommitRegistrySqlImpl(options);
    this.docLogs = new DocCommitTreeRegistrySqlImpl(options);
    this.docCommands = new DocCommandsRegistrySqlImpl(options);
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
  public DocCommitTreeRegistry docCommitTrees() {
    return docLogs;
  }
  @Override
  public DocCommandsRegistry docCommands() {
    return docCommands;
  }
  @Override
  public DocMainRegistry docs() {
    return docs;
  }

}
