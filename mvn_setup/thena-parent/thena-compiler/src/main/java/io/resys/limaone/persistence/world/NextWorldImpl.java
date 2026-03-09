package io.resys.limaone.persistence.world;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.Value;

public class NextWorldImpl implements NextWorld {
  private final Ref ref;
  private final ModelWorld world;
  private final CommitBuilder commitBuilder;
  
  private OffsetDateTime createdAt;
  private String commitAuthor = "world-builder";
  private StringBuilder commitMessage = new StringBuilder();
  private List<Deployment> deployments = new ArrayList<>();
  private int changes;
      
  public NextWorldImpl(CommitBuilder commitBuilder, Ref ref, String author, OffsetDateTime createdAt) {
    super();
    this.ref = ref;
    this.world = WorldPersistenceMapper.mapFrom(ref);
    this.commitBuilder = commitBuilder;
    this.commitAuthor = author;
    this.createdAt = createdAt;
  }

  public NextWorldResult close() {
    if(changes > 0) {
      commitBuilder
        .commitCreatedAt(createdAt)
        .commitAuthor(commitAuthor)
        .commitMessage(commitMessage.toString());
    }
    return new NextWorldResult(changes > 0, deployments.stream().findFirst());
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
    if(body.getBodyType() == BodyType.DEPLOYMENT) {
      
      RepoAssert.isTrue(deployments.isEmpty(), () -> "No point in creating more then one tag in one tx!");
      deployments.add((Deployment) body);
      
      return ImmutableModel.<T>builder()
          .id(id)
          .body(body)
          .bodyType(body.getBodyType())
          .bodyHash("not possible at a time")
          .build();
    }
    
    changes++;
    append("created new file: " + name);
    this.commitBuilder.newFile(newFile -> {
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
  @Value
  public static class NextWorldResult {
    boolean isCommits;
    Optional<Deployment> deployment;
  }
}