package io.resys.thena.docdb.file.spi;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.docdb.file.spi.FileConnection.FileTable;
import io.resys.thena.docdb.file.tables.BlobTable;
import io.resys.thena.docdb.file.tables.CommitTable;
import io.resys.thena.docdb.file.tables.RefTable;
import io.resys.thena.docdb.file.tables.RepoTable;
import io.resys.thena.docdb.file.tables.TagTable;
import io.resys.thena.docdb.file.tables.TreeItemTable;
import io.resys.thena.docdb.file.tables.TreeTable;
import io.resys.thena.docdb.spi.ClientCollections;

public class RepoTableImpl extends FileTable<RepoTable.RepoTableRow> implements RepoTable {
  private final ClientCollections ctx;
  private final BlobTable blobs;
  private final CommitTable commits;
  private final RefTable refs;
  private final TagTable tags;
  private final TreeItemTable treeItems;
  private final TreeTable trees;
  
  public RepoTableImpl(File db, ClientCollections ctx, ObjectMapper objectMapper) {
    super(db, ctx.getRepos(), objectMapper, RepoTable.RepoTableRow.class,
        new TypeReference<List<RepoTable.RepoTableRow>>() {});
    this.ctx = ctx;
    this.blobs = new BlobTableImpl(db, ctx, objectMapper);
    this.commits = new CommitTableImpl(db, ctx, objectMapper);
    this.refs = new RefTableImpl(db, ctx, objectMapper);
    this.tags = new TagTableImpl(db, ctx, objectMapper);
    this.treeItems = new TreeItemTableImpl(db, ctx, objectMapper);
    this.trees = new TreeTableImpl(db, ctx, objectMapper);
  }
  
  @Override public BlobTable getBlobs() { return blobs; }
  @Override public CommitTable getCommits() { return commits; }
  @Override public RefTable getRefs() { return refs; }
  @Override public TagTable getTags() { return tags; }
  @Override public TreeItemTable getTreeItems() { return treeItems; }
  @Override public TreeTable getTrees() { return trees; }

  public static class RefTableImpl extends FileTable<RefTable.RefTableRow> implements RefTable {
    public RefTableImpl(File db, ClientCollections ctx, ObjectMapper objectMapper) {
      super(db, ctx.getRefs(), objectMapper, RefTable.RefTableRow.class,
          new TypeReference<List<RefTable.RefTableRow>>() {}
          );
    }
  }
  public static class TreeTableImpl extends FileTable<TreeTable.TreeTableRow> implements TreeTable {
    public TreeTableImpl(File db, ClientCollections ctx, ObjectMapper objectMapper) {
      super(db, ctx.getTrees(), objectMapper, TreeTable.TreeTableRow.class,
          new TypeReference<List<TreeTable.TreeTableRow>>() {});
    }
    public List<TreeTable.TreeTableRow> insertAll(List<TreeTable.TreeTableRow> entry) {
      return super.insertAll(entry);
    }
    
    public TreeTable.TreeTableRow insert(TreeTable.TreeTableRow type) {
      return super.insert(type);
    }
  }
  public static class TreeItemTableImpl extends FileTable<TreeItemTable.TreeItemTableRow> implements TreeItemTable {
    public TreeItemTableImpl(File db, ClientCollections ctx, ObjectMapper objectMapper) {
      super(db, ctx.getTreeItems(), objectMapper, TreeItemTable.TreeItemTableRow.class,
          new TypeReference<List<TreeItemTable.TreeItemTableRow>>() {});
    }
  }  
  public static class TagTableImpl extends FileTable<TagTable.TagTableRow> implements TagTable {
    public TagTableImpl(File db, ClientCollections ctx, ObjectMapper objectMapper) {
      super(db, ctx.getTags(), objectMapper, TagTable.TagTableRow.class,
          new TypeReference<List<TagTable.TagTableRow>>() {});
    }
  }  
  public static class CommitTableImpl extends FileTable<CommitTable.CommitTableRow> implements CommitTable {
    public CommitTableImpl(File db, ClientCollections ctx, ObjectMapper objectMapper) {
      super(db, ctx.getCommits(), objectMapper, CommitTable.CommitTableRow.class,
          new TypeReference<List<CommitTable.CommitTableRow>>() {});
    }
  }  
  public static class BlobTableImpl extends FileTable<BlobTable.BlobTableRow> implements BlobTable {
    public BlobTableImpl(File db, ClientCollections ctx, ObjectMapper objectMapper) {
      super(db, ctx.getBlobs(), objectMapper, BlobTable.BlobTableRow.class,
          new TypeReference<List<BlobTable.BlobTableRow>>() {});
    }
  }
}
