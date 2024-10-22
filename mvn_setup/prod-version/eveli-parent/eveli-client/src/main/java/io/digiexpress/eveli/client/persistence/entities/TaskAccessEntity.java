package io.digiexpress.eveli.client.persistence.entities;

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

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Audited
@Table(name="task_access")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class TaskAccessEntity {
  
  public TaskAccessEntity(TaskAccessId id) {
    this.id = id;
  }

  @EmbeddedId
  private TaskAccessId id;

  @Column(nullable = false)
  private ZonedDateTime updated;

  @PrePersist
  void updateTimestamp() {
    updated = ZonedDateTime.now(ZoneId.of("UTC"));
  }
}

