package io.digiexpress.eveli.client.web.resources.worker;

/*-
 * #%L
 * eveli-client
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


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.immutables.value.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Member;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/worker/rest/api/cockpits")
@Slf4j
@RequiredArgsConstructor
public class CockpitApiController {
  private final Authoring authoring;
  
  @Value.Immutable
  interface AliasAndMember {
    Alias getAlias();
    @Nullable Member getMember();
  }
  
  @GetMapping
  public Uni<List<AliasAndMember>> findAllCockpits() {
    return Uni.combine().all()
      .unis(
        authoring.tid().aliasQuery().findAll().collect().asList(),
        authoring.tid().memberQuery().findAll().collect().asList()
      )
      .asTuple()
      .onItem().transform(tuple -> {
        final var aliases = tuple.getItem1();
        final var members = tuple.getItem2().stream().collect(Collectors.toMap(e -> e.getAliasId(), e -> e));
        return aliases.stream()
            .map(alias -> (AliasAndMember) ImmutableAliasAndMember.builder().alias(alias).member(members.get(alias.getId())).build())
            .toList();
      });
  }

  @PostMapping
  public Uni<Alias> createCockpit(@RequestBody CreateCockpitCommand command) {
    return authoring.tid().newAlias()
        .aliasName(command.getConfigName())
        .aliasDesc(command.getConfigDescription())
        .build();
  }

  @GetMapping("/{cockpitId}")
  public Uni<AliasAndMember> getOneCockpit(@PathVariable("cockpitId") String id) {
    return findAllCockpits().onItem().transform(alias -> alias.stream()
        .filter(a -> 
          a.getAlias().getId().toString().equals(id) ||
          a.getAlias().getAliasName().toString().equals(id)
        )
        .findFirst().get()
    );
  }
  

  
  @PostMapping("/activity/current-state")
  public Uni<AliasAndMember> changeActivity(@RequestBody CockpitActivityChangeActiveId change) {
    final var members = authoring.tid().memberQuery().findAllSync();
    
    final var member = members.stream().findFirst();
    final var aliasStatus = member
      .map(m -> {
        if(m.getAliasId().toString().equals(change.getActiveId())) {
          return !Boolean.TRUE.equals(m.getAliasStatus());
        }
        return true;
      })
      .orElse(true);
    
    final var upserted = authoring.tid().upsertMember()
      .aliasId(UUID.fromString(change.getActiveId()))
      .aliasStatus(aliasStatus)
      .buildSync();
    
    return getOneCockpit(change.getActiveId());
  }
  
  
  @JsonSerialize(as = ImmutableCreateCockpitCommand.class)
  @JsonDeserialize(as = ImmutableCreateCockpitCommand.class)
  @Value.Immutable
  interface CreateCockpitCommand {
    String getConfigName();
    String getConfigDescription();
  }
  
  
  @JsonSerialize(as = ImmutableCockpitActivityChangeActiveId.class)
  @JsonDeserialize(as = ImmutableCockpitActivityChangeActiveId.class)
  @Value.Immutable
  interface CockpitActivityChangeActiveId {
    @Nullable String getActiveId();
  }

}
