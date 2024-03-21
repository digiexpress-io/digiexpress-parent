package io.resys.thena.docdb.storefile.queries;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.models.ImmutableBlobHistory;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaGitObject.BlobHistory;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.models.git.GitQueries.GitBlobHistoryQuery;
import io.resys.thena.docdb.storefile.FileBuilder;
import io.resys.thena.docdb.storefile.tables.Table.FileMapper;
import io.resys.thena.docdb.storefile.tables.Table.FilePool;
import io.resys.thena.docdb.storefile.tables.TreeItemTable.TreeItemTableRow;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlobHistoryFilePool implements GitBlobHistoryQuery {

  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder builder;
  private final List<MatchCriteria> criteria = new ArrayList<>();
  private boolean latestOnly;
  private String name;


  @Override public GitBlobHistoryQuery latestOnly(boolean latestOnly) { this.latestOnly = latestOnly; return this; }
  @Override public GitBlobHistoryQuery blobName(String name) { this.name = name; return this; }
  @Override public GitBlobHistoryQuery criteria(List<MatchCriteria> criteria) { this.criteria.addAll(criteria); return this; }
  @Override public GitBlobHistoryQuery criteria(MatchCriteria... criteria) { this.criteria.addAll(Arrays.asList(criteria)); return this; }
  
  @Override
  public Multi<BlobHistory> find() {
    
    final var commits = client.preparedQuery(builder.commits().findAll()).mapping(mapper::commit).execute();
    final var blobs = client.preparedQuery(builder.blobs().findAll()).mapping(mapper::blob).execute();
    final var trees = client.preparedQuery(builder.trees().findAll()).mapping(mapper::tree).execute();
    final var treeItems = client.preparedQuery(builder.treeItems().findAll()).mapping(e -> (TreeItemTableRow) e).execute();
    
    return Uni.combine().all().unis(commits, blobs, trees, treeItems).asTuple()
      .onItem().transform(tuple -> HistoryProps.builder()
          .blobs(tuple.getItem2())
          .commits(tuple.getItem1())
          .trees(tuple.getItem3())
          .treeItems(tuple.getItem4())
          .criteria(criteria)
          .name(name)
          .latestOnly(latestOnly)
          .build())
      .onItem().transformToMulti(props -> Multi.createFrom().iterable(new HistoryVisitor(props).visit()));
    
  }
  
  @lombok.Data @lombok.Builder
  private static class HistoryProps {
    private List<Commit> commits;
    private List<Tree> trees;
    private List<TreeItemTableRow> treeItems;
    private List<Blob> blobs;
    private boolean latestOnly;
    private String name;
    private List<MatchCriteria> criteria;
  }
  
  @lombok.Data @lombok.Builder
  private static class BlobHistoryRank {
    private int rank;
    private BlobHistory value;
  }
  
  @RequiredArgsConstructor
  private static class HistoryVisitor {
    private final HistoryProps props;
    private final List<BlobHistoryRank> result = new ArrayList<>();
    private Map<String, Commit> commits;
    private Map<String, Commit> commitsByParent = new HashMap<>();
    private Commit head;
    private Map<String, Integer> blobRank = new HashMap<>();
    private Map<String, Blob> blobs;
    private Map<String, Tree> trees;
    private Map<String, List<TreeItemTableRow>> treeItems;
    
    
    public List<BlobHistory> visit() {
      this.commits = props.getCommits().stream().collect(Collectors.toMap(e -> e.getId(), (e) -> {
        final var parent = e.getParent().orElse(null);
        if(commitsByParent.containsKey(parent)) {
          throw new IllegalArgumentException("Multiple parents not supported!");
        }
        commitsByParent.put(parent, e);
        return e;
      }));
      this.blobs = props.getBlobs().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
      this.trees = props.getTrees().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
      this.treeItems = props.getTreeItems().stream().collect(Collectors.groupingBy(TreeItemTableRow::getTree));
      this.head = props.commits.stream().filter(e -> !commitsByParent.containsKey(e.getId())).findFirst().get();      

      visitCommits();
      
      return result.stream()
          .filter(e -> props.latestOnly && e.getRank() == 0 || !props.latestOnly)
          .map(e -> e.getValue())
          .collect(Collectors.toList());
    }
    

    private void visitCommits() {
      var current = head;
      do {
        
        final var tree = trees.get(current.getTree());
        for(final var treeValue : treeItems.get(tree.getId())) {
          final var blob = blobs.get(treeValue.getBlob());
          if(!BlobQueryFilePool.isMatch(blob, props.getCriteria())) {
            continue;
          }
          final var history = ImmutableBlobHistory.builder()
              .commit(current.getId())
              .treeId(tree.getId())
              .treeValueName(treeValue.getName())
              .blob(blob)
              .build();
          
          final var rank = blobRank.getOrDefault(treeValue.getName(), -1) + 1;
          blobRank.put(treeValue.getName(), rank);
          result.add(BlobHistoryRank.builder().rank(rank).value(history).build());
        }

      } while( (current = commits.get(current.getParent().orElse(null))) != null );
      
    }
  }
  
}
