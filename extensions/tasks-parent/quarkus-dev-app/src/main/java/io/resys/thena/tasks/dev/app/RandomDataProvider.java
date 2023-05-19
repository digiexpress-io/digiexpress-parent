package io.resys.thena.tasks.dev.app;

import java.time.LocalDateTime;

/*-
 * #%L
 * thena-quarkus-dev-app
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import io.resys.thena.tasks.client.api.model.ImmutableTaskComment;
import io.resys.thena.tasks.client.api.model.ImmutableTaskExtension;
import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.client.api.model.Task.Status;
import io.resys.thena.tasks.client.api.model.Task.TaskComment;
import io.resys.thena.tasks.client.api.model.Task.TaskExtension;

public class RandomDataProvider {

  private final Random rand = new Random();
  
  private final Map<Integer, String> ROLES = Map.of(
      1, "admin-role",
      2, "water-department",
      3, "education-department",
      4, "elderly-care-department",
      5, "sanitization-department"
      ); 
  

  private final Map<Integer, String> DOCS = Map.of(
      0, "file-01.pdf",
      1, "file-21.pdf",
      2, "file-6.pdf",
      3, "file-9.pdf",
      4, "file-31.pdf"
      ); 
  
  
  private final Map<Integer, String> OWNERS = Map.of(
      1, "sam vimes",
      2, "lord vetinari",
      3, "lady sybil vimes",
      4, "carrot ironfoundersson",
      5, "nobby nobbs"); 
  
  private final Map<Integer, String> SUBJECTS = Map.of(
      1, "Request for elderly care",
      2, "School application",
      3, "Sewage water disposal",
      4, "Water well construction",
      5, "General message"); 

  private final Map<Integer, String> SSN = Map.of(
      1, "Anette Lampen - 121097-676M, anette.lampen@resys.io, Jalonkatu 56, 90120, OULU",
      2, "Piia-Noora Salmelainen - 131274-780A, piia.noora.salmelainen@resys.io, tawastintie 43, 15300, LAHTI",
      3, "Arto Hakola - 170344-6999, arto.hakola@resys.io, Kajaaninkatu 29, 2120, ESPOO",
      4, "Kyllikki Multala - 270698-194T, kyllikki.multala@resys.io, Pohjoisesplanadi 49, 240, HELSINKI",
      5, "Pentti Parviainen - 171064-319U, pentti.parviainen@resys.io, Ilmalankuja 98, 28500, PORI"); 
  
  
  
  public int nextInt(final int min, final int max) {
    return rand.nextInt(max - min + 1) + min;
    
  }
  
  public String getDescription() {
    return SSN.get(nextInt(1, SSN.size()));
  }
  
  public String getSubject() {
    return SUBJECTS.get(nextInt(1, SUBJECTS.size()));
  }

  public List<String> getRoles() {
    final var roles = new ArrayList<String>();
    final var groups = nextInt(1, ROLES.size());
    for(var index = 0; index < groups; index++) {
      boolean defined = false;
      do {
        final var roleId = ROLES.get(nextInt(1, ROLES.size()));
        defined = roles.contains(roleId);
        if(!defined) {
          roles.add(roleId);
        }
      } while(defined);
    }
    return roles;
  }

  
  public List<String> getOwners() {
    final var owners = new ArrayList<String>();
    final var groups = nextInt(1, OWNERS.size()) -1;
    for(var index = 0; index < groups; index++) {
      boolean defined = false;
      do {
        final var ownerId = OWNERS.get(nextInt(1, OWNERS.size()));
        defined = owners.contains(ownerId);
        if(!defined) {
          owners.add(ownerId);
        }
      } while(defined);
    }
    return owners;
  }
  
  public List<TaskComment> getExtComments() {
    if(nextInt(1, 2) == 2) {
      final var result = new ArrayList<TaskComment>();
      final var total = nextInt(1, 5);
      for(var index = 0; index < total; index++) {
        result.add(ImmutableTaskComment.builder()
            .created(LocalDateTime.now())
            .username("random-data-gen")
            .id(UUID.randomUUID().toString())
            .commentText("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.")
            .build());
      }
    }
    
    return Collections.emptyList();    
  }
  
  public List<TaskComment> getIntComments() {
    if(nextInt(1, 2) == 2) {
      final var result = new ArrayList<TaskComment>();
      final var total = nextInt(1, 3);
      for(var index = 0; index < total; index++) {
        result.add(ImmutableTaskComment.builder()
            .created(LocalDateTime.now())
            .username("random-data-gen")
            .id(UUID.randomUUID().toString())
            .commentText("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.")
            .build());
      }
    }
    
    return Collections.emptyList();
  }
  
  public List<TaskExtension> getExtensions() {
    final var dialob = ImmutableTaskExtension.builder()
      .id(UUID.randomUUID().toString())
      .type("dialob")
      .body("dialob-content")
      .name("dialob-form")
      .build();
    if(nextInt(1, 2) == 2) {
      final var pdf = ImmutableTaskExtension.builder()
          .id(UUID.randomUUID().toString())
          .type("upload")
          .body("")
          .name(DOCS.get(nextInt(1, DOCS.size()) -1))
          .build();
      return Arrays.asList(dialob, pdf);
    }
    return Arrays.asList(dialob);
  }

  public Priority getPriority() {
    final var next = nextInt(1, 3);
    switch (next) {
    case 1: return Priority.LOW;
    case 2: return Priority.MEDIUM;
    default: return Priority.HIGH;
    }
  }
  
  public Status getStatus() {
    final var next = nextInt(1, 4);
    switch (next) {
    case 1: return Status.CREATED;
    case 2: return Status.IN_PROGRESS;
    case 3: return Status.COMPLETED;
    default: return Status.REJECTED;
    }
  }
}
