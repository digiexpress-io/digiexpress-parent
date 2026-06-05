package io.resys.limaone.persistence.world;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Description;
import io.resys.limaone.model.DescriptionLabels;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.Value;

public class NextWorldImpl implements NextWorld {
  @SuppressWarnings("unused")
  private final Optional<Ref> ref;
  private final ModelWorld world;
  private final CommitBuilder commitBuilder;
  private final String commitAuthor;
  private final OffsetDateTime createdAt;
  
  private StringBuilder commitMessage = new StringBuilder();
  private List<Deployment> newDeployment = new ArrayList<>();
  private List<Model<Deployment>> updateDeployment = new ArrayList<>();
  private List<NextWorldChange> changeCommands = new ArrayList<>();
  
  private int changes;
      
  public NextWorldImpl(CommitBuilder commitBuilder, Optional<Ref> ref, String author, OffsetDateTime createdAt) {
    super();
    this.ref = ref;
    this.world = WorldFactory.from(ref).build();
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
    return new NextWorldResult(
        changes > 0, 
        newDeployment.stream().findFirst(),
        updateDeployment.stream().findFirst()
    );
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
  public <T extends Body> Model<T> deleteModel(String id, T body) {

    changes++;
    append("deleting file: " + id);
    
    commitBuilder.remove(id);
    
    final var deleted = ImmutableModel.<T>builder()
    .id(id)
    .body(body)
    .bodyType(body.getBodyType())
    .bodyHash("not possible at a time")
    .build();
    
    this.changeCommands.add(new NextWorldChange(deleted, null, null));
    return deleted;
  }

  @Override
  public <T extends Body> Model<T> newModel(String name, T body, @Nullable Description desc, @Nullable DescriptionLabels labels) {
    final var id = OidUtils.genUUID();   
    if(body.getBodyType() == BodyType.DEPLOYMENT) {
      
      RepoAssert.isTrue(newDeployment.isEmpty() && updateDeployment.isEmpty(), () -> "No point in creating more then one tag in one tx!");
      newDeployment.add((Deployment) body);
      
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
      

      if(desc != null || labels != null) {
        newFile.fileProps(newProps -> addProps(desc, labels, newProps, Optional.empty()));
      }
      
      newFile
        .fileId(id)
        .fileName(getFileName(name, body))
        .fileValue(JsonObject.mapFrom(body))
        .fileType(body.getBodyType().name())
        .build()
      ;
    });

    final var created = ImmutableModel.<T>builder()
        .id(id)
        .body(body)
        .bodyType(body.getBodyType())
        .bodyHash("not possible at a time")
        .build();
    this.changeCommands.add(new NextWorldChange(null, null, created));
    return created;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T extends Body> Model<T> mergeModel(String id, String name, T body, @Nullable Description desc, @Nullable DescriptionLabels labels) {
    if(body.getBodyType() == BodyType.DEPLOYMENT) {
      
      RepoAssert.isTrue(newDeployment.isEmpty() && updateDeployment.isEmpty(), () -> "No point in creating more then one tag in one tx!");
      
      final var updated = ImmutableModel.<Deployment>builder()
          .id(id)
          .body((Deployment) body)
          .bodyType(body.getBodyType())
          .bodyHash("not possible at a time")
          .build();
      
      updateDeployment.add(updated);
      
      this.changeCommands.add(new NextWorldChange(null, updated, null));
      return (Model<T>) updated;
    }
    
    changes++;
    append("updated file: " + name);
    this.commitBuilder.mergeFile(id, (node, mergeFile) -> {
      
      if(desc != null || labels != null) {
        mergeFile.fileProps((prev, mergeProps) -> addProps(desc, labels, mergeProps, prev));
      }
      
      mergeFile
        .fileName(getFileName(name, body))
        .fileValue(JsonObject.mapFrom(body))
        .build();
    });
    
    final var merged = ImmutableModel.<T>builder()
        .id(id)
        .body(body)
        .bodyType(body.getBodyType())
        .bodyHash("not possible at a time")
        .build();
    
    this.changeCommands.add(new NextWorldChange(null, merged, null));
    return merged;
  }
  @Value
  public static class NextWorldResult {
    boolean isCommits;
    Optional<Deployment> newDeployment;
    Optional<Model<Deployment>> updateDeployment;
  }
  
  record NextWorldChange(Model<?> deleted, Model<?> merged, Model<?> created) {
    public BodyType getBodyType() {
      if(deleted != null) {
        return deleted.getBodyType();
      }
      if(merged != null) {
        return merged.getBodyType();
      }
      return created.getBodyType();
    }
  }
  
  
  private String getFileName(String name, Model.Body body) {
    return name;
  }
  
  private void addProps(@Nullable Description desc, @Nullable DescriptionLabels labels, PropsBuilder propsBuilder, Optional<Props> previousProps) {
    
    if(labels != null) {
      propsBuilder.propsLabels(JsonObject.mapFrom(labels));
    }
    
    if(desc != null) {
      propsBuilder.propsDescription(desc.getText());
    }
      
    propsBuilder.build();
  }
  
}
