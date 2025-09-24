package io.resys.thena.structures.fs.actions.create;

/*-
 * #%L
 * thena-docdb-api
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

import java.util.Map;

import io.resys.thena.api.entities.fs.FsDirentLink;
import io.resys.thena.api.entities.fs.ImmutableFsDirentLink;
import io.resys.thena.api.entities.fs.ImmutableFsDirentLinkTransitives;
import io.resys.thena.api.entities.fs.ThenaFsNewObject;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;


public class NewDirentLinkBuilder implements ThenaFsNewObject.NewDirentLink {
  private final FsCommitBuilder logger;
  private final Map<String, FsDirentLink> all_direntLinks;
  private ImmutableFsDirentLink.Builder next; 
  private boolean built;
  
  public NewDirentLinkBuilder(
      FsCommitBuilder logger, 
      String direntId, 
      Map<String, FsDirentLink> all_direntLinks) {
    
    super();
    this.logger = logger;
    this.next = ImmutableFsDirentLink.builder()
        .direntId(direntId)
        .commitId(logger.getCommitId())
        .createdWithCommitId(logger.getCommitId())
        .transitives(ImmutableFsDirentLinkTransitives.builder()
            .updatedAt(logger.getCreatedAt())
            .createdAt(logger.getCreatedAt())
            .build())
        .id(OidUtils.gen());
    this.all_direntLinks = all_direntLinks;
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  
  @Override
  public ThenaFsNewObject.NewDirentLink linkType(String linkType) {
    this.next.linkType(linkType);
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentLink linkValue(String linkValue) {
    this.next.linkValue(linkValue);
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentLink linkBody(JsonObject linkBody) {
    this.next.linkBody(linkBody);
    return this;
  }
  public ImmutableFsDirentLink close() {
    RepoAssert.isTrue(built, () -> "you must call LabelChanges.build() to finalize dirent CREATE or UPDATE!");
    
    final var built = next.build();
    
    RepoAssert.isTrue(
        this.all_direntLinks.values().stream()
        .filter(a -> 
          a.getLinkType().equals(built.getLinkType()) &&
          a.getLinkValue().equals(built.getLinkValue())
        )
        .count() == 0
        , () -> "can't have duplicate link of type: " + built.getLinkType() + ", with value: " + built.getLinkValue() + "!");
    
    this.logger.add(built);
    return built;
  }
}
