package io.resys.thena.registry.git;

import io.resys.thena.api.registry.GitRegistry;
import io.resys.thena.api.registry.git.BlobRegistry;
import io.resys.thena.api.registry.git.BranchRegistry;
import io.resys.thena.api.registry.git.CommitRegistry;
import io.resys.thena.api.registry.git.TagRegistry;
import io.resys.thena.api.registry.git.TreeRegistry;
import io.resys.thena.api.registry.git.TreeValueRegistry;
import io.resys.thena.datasource.TenantTableNames;

public class GitRegistrySqlImpl implements GitRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames options;
  private final BlobRegistry blobs;
  private final CommitRegistry commits;
  private final TreeValueRegistry treeValues;
  private final TreeRegistry trees;
  private final BranchRegistry branches;
  private final TagRegistry tags;
  
  public GitRegistrySqlImpl(TenantTableNames options) {
    super();
    this.options = options;
    this.blobs = new BlobRegistrySqlImpl(options);
    this.commits = new CommitRegistrySqlImpl(options);
    this.treeValues = new TreeValueRegistrySqlImpl(options);
    this.trees = new TreeRegistrySqlImpl(options);
    this.branches = new BranchRegistrySqlImpl(options);
    this.tags = new TagRegistrySqlImpl(options);
  }

  @Override
  public BlobRegistry blobs() {
    return blobs;
  }
  @Override
  public CommitRegistry commits() {
    return commits;
  }
  @Override
  public TreeValueRegistry treeValues() {
    return treeValues;
  }
  @Override
  public TreeRegistry trees() {
    return trees;
  }
  @Override
  public BranchRegistry branches() {
    return branches;
  }
  @Override
  public TagRegistry tags() {
    return tags;
  }
}
