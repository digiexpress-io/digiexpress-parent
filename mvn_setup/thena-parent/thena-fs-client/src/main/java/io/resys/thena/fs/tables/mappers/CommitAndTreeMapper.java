package io.resys.thena.fs.tables.mappers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableBlob;
import io.resys.thena.fs.entities.ImmutableNode;
import io.resys.thena.fs.entities.ImmutableNodeTransitives;
import io.resys.thena.fs.entities.ImmutableObjectIndex;
import io.resys.thena.fs.entities.ImmutableProps;
import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Tree;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

public class CommitAndTreeMapper implements TenantSql.RowMapper<Tuple2<Commit, Tree>> {
  @Override
  public Tuple2<Commit, Tree> apply(Row row) {
    
    final var commit = new CommitMapper().apply(row);
    final var visited_blobs = new ArrayList<String>();
    final var visited_props = new ArrayList<String>();
    
    final var allNodes = row.getJsonArray("nodes_json")
      .stream().map(node_json -> (JsonObject) node_json)
      .map(node_json -> {

        final var blobId = Optional.ofNullable(node_json.getString("blob_id"));
        final var propsId = Optional.ofNullable(node_json.getString("props_id"));
        final var objectId = node_json.getString("object_id");

        final var index = ImmutableObjectIndex.builder()
          .objectId(objectId)
          
          .createdAt(OffsetDateTime.parse(node_json.getString("created_at")))
          .updatedAt(OffsetDateTime.parse(node_json.getString("updated_at")))
          
          .createdBy(node_json.getString("created_by"))
          .updatedBy(node_json.getString("updated_by"))
          
          .build();

        final var nodeTrs = ImmutableNodeTransitives.builder().objectIndex(index);

        // optional when queried
        final var json_blob = node_json.getJsonObject("blob");
        if(json_blob != null && !visited_blobs.contains(blobId.get())) {

          final var blob = ImmutableBlob.builder()
            .blobType(json_blob.getString("blob_type"))
            .blobValue(json_blob.getJsonObject("blob_value"))
            .id(blobId.get())
            .build();
          
          nodeTrs.blob(blob); 
          visited_blobs.add(blob.getId());
        }
        
        // optional 
        final var json_props = node_json.getJsonObject("props");
        if(json_props != null && !visited_props.contains(propsId.get())) {
          final var props = ImmutableProps.builder()
            .id(propsId.get())
            .propsLabels(Optional.ofNullable(json_props.getJsonObject("props_labels")))
            .propsComments(Optional.ofNullable(json_props.getJsonObject("props_comments")))
            .propsPermissions(Optional.ofNullable(json_props.getJsonObject("props_permissions")))
            .propsFlags(Optional.ofNullable(json_props.getJsonObject("props_flags")))
            .build();
          nodeTrs.props(props);
          visited_props.add(props.getId());
        }
        
        // main node
        final var node = ImmutableNode.builder()
          .id(node_json.getString("id"))
          .objectId(objectId)
          .nodePath(Optional.ofNullable(node_json.getString("node_path")))
          .nodeName(node_json.getString("node_name"))
          .blobId(blobId)
          .propsId(propsId)
          .transitives(nodeTrs.build())
          .build();
        
        return node;
      })
      .toList();
    
    final var tree = ImmutableTree.builder().id(row.getString("tree_id")).treeNodes(allNodes).build();
    return Tuple2.of(commit, tree);
  }
}
