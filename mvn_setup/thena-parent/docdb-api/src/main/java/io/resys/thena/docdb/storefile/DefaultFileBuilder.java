package io.resys.thena.docdb.storefile;

import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.storefile.spi.BlobFileBuilderImpl;
import io.resys.thena.docdb.storefile.spi.CommitFileBuilderImpl;
import io.resys.thena.docdb.storefile.spi.RefFileBuilderImpl;
import io.resys.thena.docdb.storefile.spi.RepoFileBuilderImpl;
import io.resys.thena.docdb.storefile.spi.TagFileBuilderImpl;
import io.resys.thena.docdb.storefile.spi.TreeFileBuilderImpl;
import io.resys.thena.docdb.storefile.spi.TreeItemFileBuilderImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultFileBuilder implements FileBuilder {
  private final DbCollections ctx;

  @Override
  public RepoFileBuilder repo() {
    return new RepoFileBuilderImpl(ctx);
  }

  @Override
  public RefFileBuilder refs() {
    return new RefFileBuilderImpl(ctx);
  }

  @Override
  public TagFileBuilder tags() {
    return new TagFileBuilderImpl(ctx);
  }

  @Override
  public BlobFileBuilder blobs() {
    return new BlobFileBuilderImpl(ctx);
  }

  @Override
  public CommitFileBuilder commits() {
    return new CommitFileBuilderImpl(ctx);
  }

  @Override
  public TreeFileBuilder trees() {
    return new TreeFileBuilderImpl(ctx);
  }

  @Override
  public TreeItemFileBuilder treeItems() {
    return new TreeItemFileBuilderImpl(ctx);
  }

  @Override
  public FileBuilder withOptions(DbCollections options) {
    return new DefaultFileBuilder(options);
  }
}
