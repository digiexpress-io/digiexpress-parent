package io.digiexpress.eveli.client.web.resources.assets;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.immutables.value.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.spi.assets.EveliDeployment;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.model.Model.BodyType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequiredArgsConstructor
@RequestMapping("/worker/rest/api/assets/publications")
@Slf4j
public class AssetsPublicationController {
  
  private final Authoring authoring;
  private final boolean isReadOnly;
  

  @GetMapping
  public Multi<EveliDeployment> findAllPublications() {
    return authoring.worldQuery()
        .docs(BodyType.DEPLOYMENT)
        .findAll()
        .onItem().transformToMulti(world -> Multi.createFrom().items(world.getDeployments().values().stream()))
        .onItem().transform(item -> EveliDeployment.from(item, null));
  }
  
  @GetMapping("/{name}")
  public Uni<EveliDeployment> getOnePublicationByName(@PathVariable("name") String name) {
    return authoring.worldQuery()
        .docs(BodyType.DEPLOYMENT)
        .findAll()
        .onItem().transform(world -> world.getDeployments().values().stream()
          .filter(dep -> 
            dep.getId().equals(name) ||
            dep.getBody().getName().equals(name) || 
            name.equals(dep.getBody().getExternalId()))
          .findFirst()
          .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404)))
        ).onItem().transformToUni(deployment -> authoring.worldQuery()
          .docs(BodyType.without(BodyType.DEPLOYMENT))
          .commitId(deployment.getBody().getFromCommitId())
          .findAll()
          .onItem().transform(world -> EveliDeployment.from(deployment, world))
        );
  }
  
  @PostMapping
  public Uni<EveliDeployment> createOnePublication(@RequestBody CreatePublication publication) {
    if(isReadOnly) {
      throw new ResponseStatusException(HttpStatusCode.valueOf(404));
    }
    
    final OffsetDateTime now = OffsetDateTime.now();
    final OffsetDateTime liveDate = publication.getLiveDate() != null ? 
        OffsetDateTime.of(publication.getLiveDate(), ZoneOffset.UTC) : null;
    
    return authoring.newModel().newDeployment().props(props -> props
      .name(Optional.ofNullable(publication.getName()).orElse(now.toString()))
      .description(publication.getDescription())
      .liveDate(liveDate))
      .build()
      .onItem().transform(deployment -> EveliDeployment.from(deployment, null));
  }
  

  @Value.Immutable
  @JsonSerialize(as = ImmutableCreatePublication.class)
  @JsonDeserialize(as = ImmutableCreatePublication.class)
  public interface CreatePublication {
    
    @Nullable String getName();  // autoname on null
    @Nullable String getDescription();
    @Nullable LocalDateTime getLiveDate(); 
  }
}
