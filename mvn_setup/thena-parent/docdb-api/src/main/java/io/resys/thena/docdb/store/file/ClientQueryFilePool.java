package io.resys.thena.docdb.store.file;

import io.resys.thena.docdb.models.git.GitQueries;
import io.resys.thena.docdb.models.git.store.file.BlobHistoryFilePool;
import io.resys.thena.docdb.models.git.store.file.BlobQueryFilePool;
import io.resys.thena.docdb.models.git.store.file.CommitQueryFilePool;
import io.resys.thena.docdb.models.git.store.file.RefQueryFilePool;
import io.resys.thena.docdb.models.git.store.file.TagQueryFilePool;
import io.resys.thena.docdb.models.git.store.file.TreeQueryFilePool;
import io.resys.thena.docdb.store.file.tables.Table.FileClientWrapper;
import io.resys.thena.docdb.store.file.tables.Table.FileMapper;
import io.resys.thena.docdb.support.ErrorHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientQueryFilePool implements GitQueries {
  
  private final FileClientWrapper wrapper;
  private final FileMapper mapper;
  private final FileBuilder builder;
  private final ErrorHandler errorHandler;
  
  @Override
  public GitTagQuery tags() {
    return new TagQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public GitCommitQuery commits() {
    return new CommitQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public GitRefQuery refs() {
    return new RefQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public GitTreeQuery trees() {
    return new TreeQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public GitBlobQuery blobs() {
    return new BlobQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public GitBlobHistoryQuery blobHistory() {
    return new BlobHistoryFilePool(wrapper.getClient(), mapper, builder);
  }
}
