package io.resys.thena.fs.spi.tag;

/*-
 * #%L
 * thena-fs-client
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
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.fs.api.tags.TagBuilder;
import io.resys.thena.fs.entities.Entity;
import io.resys.thena.fs.entities.ImmutableTag;
import io.resys.thena.fs.entities.Tag;
import io.resys.thena.fs.entities.Tag.TagTransitives;
import io.resys.thena.fs.spi.snapshot.MutableField;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class TagBuilderImpl implements TagBuilder {
  private final Optional<Tag> prevTag;
  private final UUID commitId;
  private final Optional<UUID> refId;
  private final TagTransitives tagTransitives;  
  private final OffsetDateTime createdAt;
  private final String tagAuthor;
  
  private final MutableField<String> tagName = new MutableField<>();
  private final MutableField<String> tagDescription = new MutableField<>();
  private final MutableField<JsonObject> tagExtension = new MutableField<>();
  private final MutableField<JsonObject> tagErrors = new MutableField<>();
  private final MutableField<String> externalId = new MutableField<>();
  private final MutableField<OffsetDateTime> tagStartsAt = new MutableField<>();
  private final MutableField<JsonObject> tagReport = new MutableField<>();
  
  private boolean validated = false;

  @Override
  public TagBuilder tagName(String tagName) {
    this.tagName.withNewValue(tagName);
    return this;
  }

  @Override
  public TagBuilder tagDescription(@Nullable String tagDescription) {
    this.tagDescription.withNewValue(tagDescription);
    return this;
  }

  @Override
  public TagBuilder tagExtension(@Nullable JsonObject tagExtension) {
    this.tagExtension.withNewValue(tagExtension);
    return this;
  }

  @Override
  public TagBuilder tagErrors(@Nullable JsonObject tagErrors) {
    this.tagErrors.withNewValue(tagErrors);
    return this;
  }

  @Override
  public TagBuilder externalId(@Nullable String externalId) {
    this.externalId.withNewValue(externalId);
    return this;
  }
  @Override
  public TagBuilder tagStartsAt(@Nullable OffsetDateTime tagStartsAt) {
    this.tagStartsAt.withNewValue(tagStartsAt);
    return this;
  }

  @Override
  public TagBuilder tagReport(@Nullable JsonObject tagReport) {
    this.tagReport.withNewValue(tagReport);
    return this;
  }

  @Override
  public void build() {
    // For new tags, require at minimum tagName and tagAuthor
    if (prevTag.isEmpty()) {
      RepoAssert.isTrue(tagName.isNewValueSet(), () -> "tagName must be set for new tags");
      RepoAssert.notEmpty(tagName.getNewValue(), () -> "tagName cannot be empty");

    } else {
      // For updates, require at least one change
      RepoAssert.isTrue(
          tagName.isNewValueSet() ||
          tagDescription.isNewValueSet() ||
          tagExtension.isNewValueSet() ||
          tagErrors.isNewValueSet() ||
          externalId.isNewValueSet() ||
          tagStartsAt.isNewValueSet() ||
          tagReport.isNewValueSet(),
          () -> "cannot have empty tag merge (there are no changes)!");
    }

    this.validated = true;
       
  }
  
  public TagBuilderResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    // Merge with existing tag or use defaults for new tag
    final var finalTagName = tagName.orElse(prevTag.map(Tag::getTagName).orElse(null));
    final var finalTagDescription = tagDescription.orElse(prevTag.flatMap(Tag::getTagDescription).orElse(null));
    final var finalTagExtension = tagExtension.orElse(prevTag.flatMap(Tag::getTagExtension).orElse(null));
    final var finalTagErrors = tagErrors.orElse(prevTag.flatMap(Tag::getTagErrors).orElse(null));
    final var finalExternalId = externalId.orElse(prevTag.flatMap(Tag::getExternalId).orElse(null));
    
    final var finalTagStartsAt = tagStartsAt.orElse(prevTag.flatMap(Tag::getTagStartsAt).orElse(null));
    final var finalTagReport = tagReport.orElse(prevTag.flatMap(Tag::getTagReport).orElse(null));


    final var id = this.prevTag.map(Tag::getId).orElseGet(() -> Entity.genUUID());
    final var tag = ImmutableTag.builder()
        .id(id)
        .refId(refId)
        .commitId(commitId)
        .tagAuthor(tagAuthor)
        .tagCreatedAt(createdAt)
        .transitives(tagTransitives)
        
        .externalId(Optional.ofNullable(finalExternalId))
        
        
        .tagName(finalTagName)
        .tagDescription(Optional.ofNullable(finalTagDescription))
        
        .tagExtension(Optional.ofNullable(finalTagExtension))
        .tagErrors(Optional.ofNullable(finalTagErrors))
        .tagStartsAt(Optional.ofNullable(finalTagStartsAt))
        .tagReport(Optional.ofNullable(finalTagReport))
        
        .build();

    return new TagBuilderResult(tag);
  }
  @Value
  public static class TagBuilderResult {
    ImmutableTag tag;
  }
}
