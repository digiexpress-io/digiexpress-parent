package io.resys.thena.api.registry;

import io.resys.thena.api.registry.doc.DocBranchRegistry;
import io.resys.thena.api.registry.doc.DocCommitRegistry;
import io.resys.thena.api.registry.doc.DocLogRegistry;
import io.resys.thena.api.registry.doc.DocMainRegistry;

public interface DocRegistry {
  DocBranchRegistry docBranches();
  DocCommitRegistry docCommits();
  DocLogRegistry docLogs();
  DocMainRegistry docs();
}
