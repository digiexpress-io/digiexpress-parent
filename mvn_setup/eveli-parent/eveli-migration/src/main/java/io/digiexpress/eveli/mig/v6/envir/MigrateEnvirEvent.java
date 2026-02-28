package io.digiexpress.eveli.mig.v6.envir;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.AssetEventMigration;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.TagOperation;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MigrateEnvirEvent implements AssetEventMigration {
  private final FileSystem fs;
  private final AssetEvent event;
  
  public Uni<Void> execute() {
    if(isJunk()) {
      return Uni.createFrom().voidItem();
    }
    RepoAssert.isTrue(event.getOperations().size() == 1, () -> "Operation count must be exactly 1 but was: " + event.getOperations().size());

    final var operation = (TagOperation) event.getOperations().get(0);
    
    return commits().onItem().transform(ignore -> fs.withTenant().createTag()
        .commitId("main")
        .tagCreatedAt(operation.getCreatedAt())
        .tagAuthor(operation.getAuthor())
        .newTag(newTag -> {
          newTag
            .tagName(operation.getName())
            .tagDescription(operation.getDescription())
            .tagStartsAt(operation.getStartsAt())
            .externalId(operation.getId())
            .tagErrors(operation.getErrors().orElse(null))
            .build();
        })
        .build()
        
      ).onItem().transformToUni(junk -> Uni.createFrom().voidItem());
  }
  
  
  private Uni<Void> commits() {
    final var operation = (TagOperation) event.getOperations().get(0);
    final var sources = operation.getSources();
    final var dialob = sources.getJsonArray("dialob");
    
    if(dialob.isEmpty()) {
      return Uni.createFrom().voidItem();
    }
    
    final var commitBuilder = fs.withTenant().commitBuilder()
        .commitMessage("migration commit")
        .commitAuthor("V6 migration script")
        .commitCreatedAt(event.getCreatedAt());

    dialob.forEach(obj -> {
      
      final var form = (JsonObject) obj;
      
//      commitBuilder.newFile(newDoc -> {
//        newDoc
//        .fileType("DIALOB")
//        .fileClass("FORM")
//        .fileId(newOp.getObjectId())
//        .filePath("forms")
//        .fileName(+ ".dialob")
//        .fileValue(newOp.getNewObject())
//        .build();
//        
//      });
      
    });
    
    
    return commitBuilder.build().onItem().transformToUni(junk -> Uni.createFrom().voidItem());
  }
  
  private boolean isJunk() {
    return event.getOperations().isEmpty();
  }
}
