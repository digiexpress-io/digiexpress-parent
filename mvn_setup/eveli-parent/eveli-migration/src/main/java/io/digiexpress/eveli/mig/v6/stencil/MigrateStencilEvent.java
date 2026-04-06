package io.digiexpress.eveli.mig.v6.stencil;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.AssetEventMigration;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.MergeOperation;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.NewOperation;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.ObjectOperationType;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.RmOperation;
import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    log.info("Adding new commit: {}", event.getCreatedAt());
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
    
    var type_dirty = newOp.getNewObject().getString("type");
    if("LINK".equals(type_dirty)) {
      type_dirty = "ARTICLE_LINK";
      
    } else if("WORKFLOW".equals(type_dirty)) {
      type_dirty = "ARTICLE_WORKFLOW";
      
    } else if("PAGE".equals(type_dirty)) {
      type_dirty = "ARTICLE_PAGE";
      
    } else if("TEMPLATE".equals(type_dirty)) {
      type_dirty = "ARTICLE_TEMPLATE";
    }
    
    
    final var type = type_dirty;
    commitBuilder.newFile(newFile -> {
      newFile
        .fileType(type)
        .fileId(newOp.getObjectId())
        .fileName(newOp.getObjectId())
        //.fileClass(type)
        //.filePath(getFilePath(newOp.getObjectId(), newOp.getNewObject(), newOp.getSourceTree(), newOp.getOriginalCommitId()))
        //.fileName(getFileName(newOp.getObjectId(), newOp.getNewObject(), newOp.getSourceTree(), newOp.getOriginalCommitId()) + "." + type.toLowerCase())
        .fileValue(migrateAsset(newOp.getNewObject()))
        .build();
    });
  }
  private void doMerge(CommitBuilder commitBuilder, MergeOperation mergeOp) {
    if(isJunk(mergeOp.getNewObject())) {
      return;
    }
    commitBuilder.mergeFile(mergeOp.getObjectId(),(pre, mergeFile) -> {
      mergeFile
        //.filePath(getFilePath(mergeOp.getObjectId(), mergeOp.getNewObject(), mergeOp.getSourceTree(), mergeOp.getOriginalCommitId()))
        //.fileName(getFileName(mergeOp.getObjectId(), mergeOp.getNewObject(), mergeOp.getSourceTree(), mergeOp.getOriginalCommitId()) + "." + type.toLowerCase())
        .fileValue(migrateAsset(mergeOp.getNewObject()))
        .build();
    });
  }

  private JsonObject migrateAsset(JsonObject start) {
    final var type = start.getString("type");
    final var body = start.getJsonObject("body");
    
    switch(type) {
      case "WORKFLOW": {
        final var startDate = toOffsetDateTime(body.getString("startDate"));
        final var endDate = toOffsetDateTime(body.getString("endDate"));
        body.put("startDate", startDate);
        body.put("endDate", endDate);
      };
      default: break;
    }
    return body;
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
  
  private String getFileName(String objectId, JsonObject content, OldGit.OldGitObjects git, String commitId) {
    final var type = content.getString("type");
    final var body = content.getJsonObject("body");
    switch(type) {
      case "LOCALE": return body.getString("value");
      case "LINK": return objectId;
      case "ARTICLE": return body.getString("name");
      case "WORKFLOW": return body.getString("value");
      case "PAGE": {
        final var localeId = body.getString("locale");
        final var locale = git.getBlob(commitId, localeId);
        return locale.getValue().getJsonObject("body").getString("value");
      }
      case "TEMPLATE": return objectId;
      default: throw new IllegalArgumentException("Unknown docType: " + type);
    }
  }
  
  private String getFilePath(String objectId, JsonObject content, OldGit.OldGitObjects git, String commitId) {
    final var type = content.getString("type");
    final var body = content.getJsonObject("body");
    
    switch(type) {
      case "LOCALE": return "stencil/locales";
      case "LINK": return "stencil/links";
      case "ARTICLE": return getArticleFolderName(objectId, content, git, commitId);
      case "PAGE": {
        final var articleId = body.getString("article");        
        final var article = git.getBlob(commitId, articleId);
        return getArticleFolderName(objectId, article.getValue(), git, commitId);
      }
      case "WORKFLOW": return "stencil/workflows";
      case "TEMPLATE": return "stencil/templates";
      default: throw new IllegalArgumentException("Unknown docType: " + type);
    }
  }
  

  private String getArticleFolderName(String objectId, JsonObject content, OldGit.OldGitObjects git, String commitId) {
    final var body = content.getJsonObject("body");
    
    String folderName = "";
    JsonObject article = body;
    var index = 0;
    while(article != null) {
      if(index > 2) {
        throw new RuntimeException("cant resolve article folder name");
      }
      index++;
      
      final var articleOrder = String.format("%03d", body.getInteger("order"));
      final var articleName = body.getString("name");    
      
      if(!folderName.isEmpty()) {
        folderName += "/";
      }
      folderName += articleOrder + "_" + articleName;
      
      // next article
      final var parentId = article.getString("parentId");
      if(parentId == null || parentId.isEmpty()) {
        break;
      } 
      final var blob = git.getBlob(commitId, parentId);
      article = RepoAssert.notNull(blob.getValue().getJsonObject("body"), () -> "Failed to resolve blob!");
    }

    return "stencil/articles/" + folderName;
  }
  
  private OffsetDateTime toOffsetDateTime(String date_text) {
    if(date_text == null) {
      return null;
    }
    
    try {
      final var date = LocalDateTime.parse(date_text);
      return OffsetDateTime.of(date, ZoneOffset.UTC);
    } catch(Exception e) {
      return OffsetDateTime.parse(date_text);      
    }
  }
}
