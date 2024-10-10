package io.resys.thena.api.registry;

import io.resys.thena.api.registry.doc.DocBranchRegistry;
import io.resys.thena.api.registry.doc.DocCommandsRegistry;
import io.resys.thena.api.registry.doc.DocCommitRegistry;
import io.resys.thena.api.registry.doc.DocCommitTreeRegistry;
import io.resys.thena.api.registry.doc.DocMainRegistry;

public interface DocRegistry {
  DocBranchRegistry docBranches();
  DocCommitRegistry docCommits();
  DocCommitTreeRegistry docCommitTrees();
  DocCommandsRegistry docCommands();
  DocMainRegistry docs();
}
