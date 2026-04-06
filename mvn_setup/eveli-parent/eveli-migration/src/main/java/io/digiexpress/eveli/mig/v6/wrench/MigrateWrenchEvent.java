package io.digiexpress.eveli.mig.v6.wrench;

import java.util.Arrays;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.AssetEventMigration;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.MergeOperation;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.NewOperation;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.ObjectOperationType;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.RmOperation;
import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.ImmutableFlow;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class MigrateWrenchEvent implements AssetEventMigration {
  private final FileSystem fs;
  private final AssetEvent event;
  private final AST_Parser astParser = AST_ParserImpl.builder().build();
  
  public MigrateWrenchEvent(FileSystem fs, AssetEvent event) {
    super();
    this.fs = fs;
    this.event = event;
  }

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
    
    var type_dirty = newOp.getNewObject().getString("bodyType");
    if("DT".equals(type_dirty)) {
      type_dirty = "DECISION_TABLE";
    }
    final var type = type_dirty;
    
    commitBuilder.newFile(newFile -> {
      newFile
        .filePath(getFilePath(newOp.getObjectId(), newOp.getNewObject()))
        .fileType(type)
        .fileId(newOp.getObjectId())
        .fileName(newOp.getObjectId())
        .fileValue(migrateAsset(newOp.getNewObject()))
        //.fileClass(type)
        //.fileName(getFileName(newOp.getObjectId(), newOp.getNewObject()) + "." + type.toLowerCase())
        .build();
    });
  }
  private void doMerge(CommitBuilder commitBuilder, MergeOperation mergeOp) {
    if(isJunk(mergeOp.getNewObject())) {
      return;
    }
    
    commitBuilder.mergeFile(mergeOp.getObjectId(),(pre, mergeFile) -> {
      mergeFile
        .fileValue(migrateAsset(mergeOp.getNewObject()))
        //.fileName(getFileName(mergeOp.getObjectId(), mergeOp.getNewObject()) + "." + type.toLowerCase())
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
    final var type = content.getString("bodyType");
    return !Arrays.asList("FLOW_TASK", "DT", "FLOW").contains(type);
  }
  
  private JsonObject migrateAsset(JsonObject start) {
    final var type = start.getString("bodyType");
    final var commands = start.getJsonArray("body");
    final var setBody = commands.stream()
        .map(e -> (JsonObject) e)
        .filter(e -> e.getString("type").equals("SET_BODY"))
        .toList();
    
    switch(type) {
      case "FLOW_TASK": {
        RepoAssert.isTrue(setBody.size() == 1, () -> "SET_BODY must have type 1");
        final var first = setBody.getFirst().getString("value");
        final var ast = astParser.parseFlowTask().syntax(first).parse();
        final var task = ImmutableFlowTask.builder().taskName(ast.getName()).taskValue(first).build();
        return JsonObject.mapFrom(task);
      }
      case "FLOW": {
        RepoAssert.isTrue(setBody.size() == 1, () -> "SET_BODY must have type 1");
        final var first = setBody.getFirst().getString("value");
        final var ast = astParser.parseFlow().syntax(first).parse();
        final var flow = ImmutableFlow.builder().flowName(ast.getName()).flowValue(first).build();
        return JsonObject.mapFrom(flow);
      }
      
      case "DT": {
        final var first = commands.stream().map(e -> (JsonObject) e)
            .map(e -> e.mapTo(DecisionStatement.class))
            .toList();
        final var ast = astParser.parseDecisionTable().nodes(first).parse();
        final var flow = ImmutableDecisionTable.builder()
            .name(ast.getName())
            .nodes(first)
            .build();
        return JsonObject.mapFrom(flow);
      }
      default: throw new RuntimeException("unknown asset type: " + type);
    }
  }
  
  private String getFileName(String objectId, JsonObject content) {
    final var type = content.getString("bodyType");
    final var body = content.getJsonArray("body");
        
    switch(type) {
      case "FLOW": { 
        final var value = body.stream()
            .map(e -> (JsonObject) e)
            .filter(e -> e.getString("type").equals("SET_BODY"))
            .findFirst();
        final var flow = astParser.parseFlow().syntax(value.get().getString("value")).parse();
        return flow.getName();
      }
      case "DT": {
        final var nodes = body.stream()
            .map(e -> (JsonObject) e)
            .map(e -> e.mapTo(DecisionStatement.class))
            .toList();
        
        final var flow = astParser.parseDecisionTable().nodes(nodes).parse();
        return flow.getName();
      }
      case "FLOW_TASK": {
        final var value = body.stream()
            .map(e -> (JsonObject) e)
            .filter(e -> e.getString("type").equals("SET_BODY"))
            .findFirst();
        final var flowTask = astParser.parseFlowTask().syntax(value.get().getString("value")).parse();
        return flowTask.getName();
      }

      default: throw new IllegalArgumentException("Unknown docType: " + type);
    }
  }
  
  private String getFilePath(String objectId, JsonObject content) {
    final var type = content.getString("bodyType");
    return "wrench/"+ type.toLowerCase();
  }
}
