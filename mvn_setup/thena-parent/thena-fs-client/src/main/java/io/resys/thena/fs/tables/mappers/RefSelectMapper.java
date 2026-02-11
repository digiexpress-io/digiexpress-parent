package io.resys.thena.fs.tables.mappers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;

import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableBlob;
import io.resys.thena.fs.entities.ImmutableCommit;
import io.resys.thena.fs.entities.ImmutableNode;
import io.resys.thena.fs.entities.ImmutableNodeTransitives;
import io.resys.thena.fs.entities.ImmutableObjectIndex;
import io.resys.thena.fs.entities.ImmutableProps;
import io.resys.thena.fs.entities.ImmutableRef;
import io.resys.thena.fs.entities.ImmutableRefTransitives;
import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.TableUtils;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

public class RefSelectMapper {
  
  public static Ref refAndCommitAndPropsAndBlob(Row row) {
    
    final var refTransitives = ImmutableRefTransitives.builder();
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
          refTransitives.putBlobsById(blob.getId(), blob);            
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
          refTransitives.putPropsById(props.getId(), props);
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
        
        refTransitives.putNodesByPath(node.getFullPath(), node.getId());
        refTransitives.putNodesById(node.getId(), node);
        
        
        return node;
      })
      .toList();
    
    final var tree = ImmutableTree.builder().id(row.getString("tree_id")).treeNodes(allNodes).build();
    final var baseline = refAndCommit(row);
    
    return baseline.getItem1()
        .transitives(refTransitives.commit(baseline.getItem2()).tree(tree).build())
        .build();
  }
  

  // just load basic data
  public static Tuple2<ImmutableRef.Builder, Commit> refAndCommit(Row row) {
    final var refBuilder = ImmutableRef.builder()
        .id(TableUtils.toStringUUID(row, "id"))
        .refName(row.getString("ref_name"))
        .commitId(row.getString("commit_id"));
    
    // Add optional ref properties
    final String refDescription = row.getString("ref_description");
    if (refDescription != null) {
      refBuilder.branchDescription(refDescription);
    }
    
    final JsonObject refProps = row.getJsonObject("ref_props");
    if (refProps != null) {
      refBuilder.branchProps(refProps);
    }
    
    final JsonObject refPermissions = row.getJsonObject("ref_permissions");
    if (refPermissions != null) {
      refBuilder.branchPermissions(refPermissions);
    }
    
    final JsonObject refFlags = row.getJsonObject("ref_flags");
    if (refFlags != null) {
      refBuilder.branchFlags(refFlags);
    }
    
    final String refAuthor = row.getString("ref_author");
    if (refAuthor != null) {
      refBuilder.branchAuthor(refAuthor);
    }
    
    // Build commit object from joined data
    final String commitId = row.getString("commit_id");
    
  
    final var commitBuilder = ImmutableCommit.builder()
        .id(commitId)
        .commitCreatedAt(row.getOffsetDateTime("commit_created_at"))
        .commitAuthor(row.getString("commit_author"))
        .commitMessage(row.getString("commit_message"))
        .treeId(row.getString("tree_id"));
    
    final String parentId = row.getString("parent_id");
    if (parentId != null) {
      commitBuilder.parentId(parentId);
    }
    
    final String mergeId = row.getString("merge_id");
    if (mergeId != null) {
      commitBuilder.mergeId(mergeId);
    }
    
    final var commit = commitBuilder.build();
    return Tuple2.of(refBuilder, commit);
  }
}
