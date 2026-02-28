package io.digiexpress.eveli.mig.v6.wrench;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.AssetEventMigration;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.MergeOperation;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.NewOperation;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.ObjectOperationType;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.RmOperation;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class MigrateWrenchEvent implements AssetEventMigration {
  private final FileSystem fs;
  private final AssetEvent event;
  private final HdesClient client;
  private static final ObjectMapper objectMapper = new ObjectMapper()
      .registerModules(
          new JavaTimeModule(), 
          new Jdk8Module(), 
          new GuavaModule());
  
  public MigrateWrenchEvent(FileSystem fs, AssetEvent event) {
    super();
    this.fs = fs;
    this.event = event;
    
    this.client = HdesClientImpl.builder()
        .objectMapper(objectMapper)
        .store(new HdesInMemoryStore(new HashMap<>()))
        .dependencyInjectionContext(new DependencyInjectionContext() {
          @Override
          public <T> T get(Class<T> type) {
            return null;
          }
        })
        .serviceInit(new ServiceInit() {
            @Override
            public <T> T get(Class<T> type) {
              try {
                return type.getDeclaredConstructor().newInstance();
              } catch(Exception e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            }
          })
        .build();
    
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
    
    final var type = newOp.getNewObject().getString("bodyType");
    commitBuilder.newFile(newFile -> {
      newFile
        .filePath(getFilePath(newOp.getObjectId(), newOp.getNewObject()))
        .fileType("WRENCH")
        .fileClass(type)
        .fileId(newOp.getObjectId())
        .fileName(getFileName(newOp.getObjectId(), newOp.getNewObject()) + "." + type.toLowerCase())
        .fileValue(newOp.getNewObject())
        .build();
    });
  }
  private void doMerge(CommitBuilder commitBuilder, MergeOperation mergeOp) {
    final var type = mergeOp.getNewObject().getString("bodyType");
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
    final var type = content.getString("bodyType");
    return "TAG".equals(type);
  }
  
  private String getFileName(String objectId, JsonObject content) {
    final var type = content.getString("bodyType");
    final var body = content.getJsonArray("body").encode();
    final var ast = client.ast().commands(body);
    
    
    switch(type) {
      case "FLOW": { 
        final var flow = ast.flow();
        return flow.getName();
      }
      case "DT": {
        final var flow = ast.decision();
        return flow.getName();        
      }
      case "FLOW_TASK": {
        final var flow = ast.service();
        return flow.getName();
      }

      default: throw new IllegalArgumentException("Unknown docType: " + type);
    }
  }
  
  private String getFilePath(String objectId, JsonObject content) {
    final var type = content.getString("bodyType");
    return "wrench/"+ type.toLowerCase();
  }
}
