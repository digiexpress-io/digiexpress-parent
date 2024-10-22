package io.digiexpress.eveli.client.spi;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;

import io.digiexpress.eveli.client.api.AssetReleaseCommands;
import io.digiexpress.eveli.client.api.DialobCommands;
import io.digiexpress.eveli.client.api.ImmutableAssetReleaseTag;
import io.digiexpress.eveli.client.api.ImmutableReleaseAssets;
import io.digiexpress.eveli.client.api.JsonNodeTagCommands;
import io.digiexpress.eveli.client.api.WorkflowCommands;
import io.digiexpress.eveli.client.api.WorkflowCommands.WorkflowTag;
import io.digiexpress.eveli.client.persistence.repositories.AssetReleaseRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Builder
@Slf4j
public class AssetReleaseCommandsImpl implements AssetReleaseCommands {
  
  @lombok.NonNull
  private final AssetReleaseRepository repository;
  @lombok.NonNull
  private final WorkflowCommands workflow;
  private final JsonNodeTagCommands<AssetTag> wrenchTags;
  private final JsonNodeTagCommands<AssetTag> contentTags;
  @lombok.NonNull
  private final DialobCommands dialobCommands;

  @Override
  public AssetReleaseTag createTag(AssetReleaseTagInit release) {
    final var releaseBuilder = io.digiexpress.eveli.client.persistence.entities.AssetReleaseEntity.builder()
        .name(release.getName())
        .description(release.getDescription())
        .createdBy(release.getUser());
    
    if (StringUtils.isAllBlank(release.getContentTag())) {
      createContentTag(release);
      releaseBuilder.contentTag(release.getName());
    }
    else {
      releaseBuilder.contentTag(release.getContentTag());
    }
    if (StringUtils.isAllBlank(release.getWorkflowTag())) {
      createWorkflowRelease(release);
      releaseBuilder.workflowTag(release.getName());
    }
    else {
      releaseBuilder.workflowTag(release.getWorkflowTag());
    }
    if (StringUtils.isAllBlank(release.getWrenchTag())) {
      createWrenchRelease(release);
      releaseBuilder.wrenchTag(release.getName());
    }
    else {
      releaseBuilder.wrenchTag(release.getWrenchTag());
    }
    
    final var assetRelease = repository.save(releaseBuilder.build());
    
    return toImmutableTag(assetRelease);
  }

  @Override
  public Optional<AssetReleaseTag> getByName(String name) {
    return repository.findByName(name).map(tag-> toImmutableTag(tag));
  }

  @Override
  public List<AssetReleaseTag> findAll() {
    return StreamSupport.stream(repository.findAll().spliterator(), false)
        .map(tag->toImmutableTag(tag))
        .collect(Collectors.toList());
  } 

  @Override
  public AssetReleaseTag createTag(AssetTagInit init) {
    return createTag((AssetReleaseTagInit)init);
  }

  @Override
  public List<AssetTag> getWrenchTags() {
    if (wrenchTags == null) {
      return Collections.emptyList();
    }
    return wrenchTags.findAll();
  }

  @Override
  public List<? extends AssetTag> getWorkflowTags() {
    return workflow.release().findAll();
  }

  @Override
  public List<AssetTag> getContentTags() {
    if (contentTags == null) {
      return Collections.emptyList();
    }
    return contentTags.findAll();
  }
  
  @Override
  public Optional<ReleaseAssets> getAssetRelease(String releaseName) {
    log.info("Download request for release {}", releaseName);
    Optional<AssetReleaseTag> assetRelease = getByName(releaseName);
    if (assetRelease.isEmpty()) {
      return Optional.empty();
    }
    AssetReleaseTag assetReleaseTag = assetRelease.get();
    ImmutableReleaseAssets.Builder builder = ImmutableReleaseAssets.builder();
    log.debug("Getting asset release");
    builder.assetRelease(assetReleaseTag);
    log.debug("Getting workflow release");
    WorkflowTag workflowTag = workflow.release().getByName(assetReleaseTag.getWorkflowTag()).orElse(null);
    builder.workflowRelease(workflowTag);
    log.debug("Getting content release");
    if (contentTags != null) {
      builder.contentRelease(contentTags.getTagAssets(assetReleaseTag.getContentTag()));
    }
    log.debug("Getting wrench release");
    if (wrenchTags != null) {
      builder.wrenchRelease(wrenchTags.getTagAssets(assetReleaseTag.getWrenchTag()));
    }
    log.debug("Release download created");
    return Optional.of(builder.build());
  }
  
  private void createWrenchRelease(AssetTagInit assetTag) {
    if (wrenchTags != null) {
      wrenchTags.createTag(assetTag);
    }
  }

  private void createWorkflowRelease(AssetTagInit assetTag) {
    workflow.release().createTag(assetTag);
  }

  private void createContentTag(AssetTagInit assetTag) {
    if (contentTags != null) {
      contentTags.createTag(assetTag);
    }
  }

  private ImmutableAssetReleaseTag toImmutableTag(io.digiexpress.eveli.client.persistence.entities.AssetReleaseEntity tag) {
    return ImmutableAssetReleaseTag.builder()
        .id(tag.getId().toString())
        .name(tag.getName())
        .description(tag.getDescription())
        .created(tag.getCreated().toLocalDateTime())
        .user(tag.getCreatedBy())
        .contentTag(tag.getContentTag())
        .workflowTag(tag.getWorkflowTag())
        .wrenchTag(tag.getWrenchTag())
        .build();
  }

}
