package io.resys.limaone.persistence;

import java.time.OffsetDateTime;

import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.resys.thena.fs.api.FileSystem.FileSystemTenant;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.OidUtils;
import io.vertx.core.json.JsonObject;

public class NextWorldImpl implements NextWorld {
  private final FileSystemTenant tenant;
  private final Ref ref;
  private final ModelWorld world;
  private final CommitBuilder commitBuilder;
  
  private OffsetDateTime createdAt;
  private String commitAuthor = "world-builder";
  private StringBuilder commitMessage = new StringBuilder();
      
  public NextWorldImpl(FileSystemTenant tenant, Ref ref, String author, OffsetDateTime createdAt) {
    super();
    this.tenant = tenant;
    this.ref = ref;
    this.world = WorldPersistenceMapper.mapFrom(ref);
    this.commitBuilder = tenant.commitBuilder();
    this.commitAuthor = author;
    this.createdAt = createdAt;
  }

  public CommitBuilder close() {
    return commitBuilder
        .commitCreatedAt(createdAt)
        .commitAuthor(commitAuthor)
        .commitMessage(commitMessage.toString());
  }
  
  private void append(String msg) {
    if(!commitMessage.isEmpty()) {
      commitMessage.append(System.lineSeparator());
    }
    commitMessage.append(msg);
  }

  @Override
  public ModelWorld getCurrentWorld() {
    return world;
  }

  @Override
  public <T extends Body> Model<T> newModel(String name, T body) {
    final var id = OidUtils.genUUID();
    this.commitBuilder.newFile(newFile -> {
      
      append("created new file: " + name);
      
      newFile
        .fileId(id)
        .fileName(name)
        .fileValue(JsonObject.mapFrom(body))
        .fileType(body.getBodyType().name())
        .build()
      ;
    });

    
    return ImmutableModel.<T>builder()
        .id(id)
        .body(body)
        .bodyType(body.getBodyType())
        .bodyHash("not possible at a time")
        .build();
  }
  
  
  
  @Override
  public <T extends Body> Model<T> mergeModel(String id, String name, T body) {
    append("updated file: " + name);
    return null;
  }
}