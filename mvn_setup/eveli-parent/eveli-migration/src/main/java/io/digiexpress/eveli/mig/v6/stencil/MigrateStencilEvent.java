package io.digiexpress.eveli.mig.v6.stencil;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.AssetEventMigration;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.MergeOperation;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.NewOperation;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.ObjectOperationType;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.RmOperation;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MigrateStencilEvent implements AssetEventMigration {
  private final FileSystem fs;
  private final AssetEvent event;
  
  public Uni<Void> execute() {
    
    if(isJunk()) {
      return Uni.createFrom().voidItem();
    }
    
    
    final var commitBuilder = fs.withTenant().commitBuilder()
      .commitMessage("migration commit")
      .commitAuthor("V6 migration script")
      .commitCreatedAt(event.getCreatedAt());
    
    for(final var operation : event.getOperations()) {
      switch(operation.getType()) {
        case RM_OBJECT: doRm(commitBuilder, (RmOperation) operation); break;
        case NEW_OBJECT: doNew(commitBuilder, (NewOperation) operation); break;
        case MERGE_OBJECT: doMerge(commitBuilder, (MergeOperation) operation); break;
        case TAG_OBJECTS: throw new RuntimeException("Tagging must come from envir!");
      }
    }

    return commitBuilder.build().onItem().transformToUni(log -> {
      return Uni.createFrom().voidItem();
    });
  }
  
  private void doRm(CommitBuilder commitBuilder, RmOperation rmOp) {
    commitBuilder.remove(rmOp.getObjectId());
  }
  private void doNew(CommitBuilder commitBuilder, NewOperation newOp) {
    if(isJunk(newOp.getNewObject())) {
      return;
    }
    
    final var type = newOp.getNewObject().getString("type");
    commitBuilder.newFile(newFile -> {
      newFile
        .fileType("STENCIL")
        .fileClass(type)
        .fileId(newOp.getObjectId())
        .fileName(getFileName(newOp.getObjectId(), newOp.getNewObject()) + "." + type.toLowerCase())
        .fileValue(newOp.getNewObject())
        .build();
    });
  }
  private void doMerge(CommitBuilder commitBuilder, MergeOperation mergeOp) {
    final var type = mergeOp.getNewObject().getString("type");
    commitBuilder.mergeFile(mergeOp.getObjectId(),(pre, mergeFile) -> {
      mergeFile
        .fileName(getFileName(mergeOp.getObjectId(), mergeOp.getNewObject()) + "." + type.toLowerCase())
        .fileValue(mergeOp.getNewObject())
        .build();
    });
  }
  
  private boolean isJunk() {
    if(event.getOperations().size() == 1) {    
      final var op = event.getOperations().get(1);
      if(op.getType() == ObjectOperationType.NEW_OBJECT) {
        final var newOp = (NewOperation) op;
        return isJunk(newOp.getNewObject());
      }
    }
    
    return false;
  }
  
  private boolean isJunk(JsonObject content) {
    final var type = content.getString("type");
    return "RELEASE".equals(type);
  }
  
  private String getFileName(String objectId, JsonObject content) {
    final var type = content.getString("type");
    final var body = content.getJsonObject("body");
    switch(type) {
      case "LOCALE": return body.getString("value");
      case "LINK": return objectId;
      case "ARTICLE": return body.getString("name");
      case "WORKFLOW": return body.getString("value");
      case "PAGE": return objectId;
      case "TEMPLATE": return objectId;
      default: throw new IllegalArgumentException("Unknown docType: " + type);
    }
  }
}
