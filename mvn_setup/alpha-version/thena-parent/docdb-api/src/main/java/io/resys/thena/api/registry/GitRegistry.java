package io.resys.thena.api.registry;

import io.resys.thena.api.registry.git.BlobRegistry;
import io.resys.thena.api.registry.git.BranchRegistry;
import io.resys.thena.api.registry.git.CommitRegistry;
import io.resys.thena.api.registry.git.TagRegistry;
import io.resys.thena.api.registry.git.TreeRegistry;
import io.resys.thena.api.registry.git.TreeValueRegistry;

public interface GitRegistry {
  BlobRegistry blobs();
  CommitRegistry commits();
  TreeValueRegistry treeValues();
  TreeRegistry trees();
  BranchRegistry branches();
  TagRegistry tags();
}
